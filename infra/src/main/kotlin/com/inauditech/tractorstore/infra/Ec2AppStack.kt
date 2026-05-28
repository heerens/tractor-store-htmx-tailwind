package com.inauditech.tractorstore.infra

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator
import software.amazon.awscdk.services.cloudwatch.Metric
import software.amazon.awscdk.services.cloudwatch.TreatMissingData
import software.amazon.awscdk.services.cloudwatch.actions.Ec2Action
import software.amazon.awscdk.services.cloudwatch.actions.Ec2InstanceAction
import software.amazon.awscdk.services.ec2.AmazonLinux2023ImageSsmParameterProps
import software.amazon.awscdk.services.ec2.AmazonLinuxCpuType
import software.amazon.awscdk.services.ec2.BlockDevice
import software.amazon.awscdk.services.ec2.BlockDeviceVolume
import software.amazon.awscdk.services.ec2.CfnEIP
import software.amazon.awscdk.services.ec2.CfnEIPAssociation
import software.amazon.awscdk.services.ec2.EbsDeviceOptions
import software.amazon.awscdk.services.ec2.EbsDeviceVolumeType
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Instance
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.MachineImage
import software.amazon.awscdk.services.ec2.Peer
import software.amazon.awscdk.services.ec2.Port
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType
import software.amazon.awscdk.services.ec2.UserData
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.ssm.CfnMaintenanceWindow
import software.amazon.awscdk.services.ssm.CfnMaintenanceWindowTarget
import software.amazon.awscdk.services.ssm.CfnMaintenanceWindowTask
import software.constructs.Construct

class Ec2AppStack(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {

    val vpc: IVpc = Vpc.fromLookup(
        this,
        "DefaultVpc",
        VpcLookupOptions.builder().isDefault(true).build(),
    )

    val securityGroup: SecurityGroup = SecurityGroup.Builder.create(this, "InstanceSg")
        .vpc(vpc)
        .description("Tractor Store EC2: allow inbound 80/443 (Caddy)")
        .allowAllOutbound(true)
        .build()

    val instanceRole: Role = Role.Builder.create(this, "InstanceRole")
        .roleName(Config.EC2_INSTANCE_ROLE_NAME)
        .assumedBy(ServicePrincipal("ec2.amazonaws.com"))
        .description("Runtime IAM role for tractor-store-app on EC2 (Caddy + Docker)")
        .managedPolicies(
            listOf(
                ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"),
            ),
        )
        .build()

    val logGroup: LogGroup = LogGroup.Builder.create(this, "AppLogGroup")
        .logGroupName(Config.EC2_LOG_GROUP_NAME)
        .retention(RetentionDays.ONE_MONTH)
        .removalPolicy(RemovalPolicy.RETAIN)
        .build()

    val instance: Instance
    val eip: CfnEIP

    init {
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "HTTP for ACME challenge + redirect")
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "HTTPS")

        // This app is a self-contained demo: no DynamoDB, no S3, no Cognito. The instance role
        // only needs to pull the image from ECR and ship container logs to CloudWatch.
        instanceRole.addToPolicy(
            PolicyStatement.Builder.create()
                .actions(listOf("ecr:GetAuthorizationToken"))
                .resources(listOf("*"))
                .build(),
        )
        instanceRole.addToPolicy(
            PolicyStatement.Builder.create()
                .actions(
                    listOf(
                        "ecr:BatchCheckLayerAvailability",
                        "ecr:GetDownloadUrlForLayer",
                        "ecr:BatchGetImage",
                    ),
                )
                .resources(
                    listOf("arn:aws:ecr:${Config.REGION}:${Config.ACCOUNT}:repository/${Config.ECR_REPO_NAME}"),
                )
                .build(),
        )
        instanceRole.addToPolicy(
            PolicyStatement.Builder.create()
                .actions(
                    listOf(
                        "logs:CreateLogStream",
                        "logs:PutLogEvents",
                        "logs:DescribeLogStreams",
                    ),
                )
                .resources(
                    listOf("arn:aws:logs:${Config.REGION}:${Config.ACCOUNT}:log-group:${Config.EC2_LOG_GROUP_NAME}:*"),
                )
                .build(),
        )

        val machineImage = MachineImage.latestAmazonLinux2023(
            AmazonLinux2023ImageSsmParameterProps.builder()
                .cpuType(AmazonLinuxCpuType.ARM_64)
                .build(),
        )

        val userData = UserData.custom(renderBootstrapScript())

        instance = Instance.Builder.create(this, "AppInstance")
            .vpc(vpc)
            .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE4_GRAVITON, InstanceSize.SMALL))
            .machineImage(machineImage)
            .securityGroup(securityGroup)
            .role(instanceRole)
            .userData(userData)
            .requireImdsv2(true)
            .blockDevices(
                listOf(
                    BlockDevice.builder()
                        .deviceName("/dev/xvda")
                        .volume(
                            BlockDeviceVolume.ebs(
                                20,
                                EbsDeviceOptions.builder()
                                    .volumeType(EbsDeviceVolumeType.GP3)
                                    .encrypted(true)
                                    .deleteOnTermination(true)
                                    .build(),
                            ),
                        )
                        .build(),
                ),
            )
            .build()

        Tags.of(instance).add("Name", Config.EC2_INSTANCE_NAME)

        eip = CfnEIP.Builder.create(this, "AppEip")
            .domain("vpc")
            .build()

        CfnEIPAssociation.Builder.create(this, "AppEipAssociation")
            .allocationId(eip.attrAllocationId)
            .instanceId(instance.instanceId)
            .build()

        val statusCheckAlarm = Alarm.Builder.create(this, "SystemStatusCheckAlarm")
            .metric(
                Metric.Builder.create()
                    .namespace("AWS/EC2")
                    .metricName("StatusCheckFailed_System")
                    .dimensionsMap(mapOf("InstanceId" to instance.instanceId))
                    .period(Duration.minutes(1))
                    .statistic("Maximum")
                    .build(),
            )
            .threshold(0.0)
            .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
            .evaluationPeriods(2)
            .datapointsToAlarm(2)
            .treatMissingData(TreatMissingData.NOT_BREACHING)
            .alarmDescription("Recover the instance when the underlying hardware fails")
            .build()
        statusCheckAlarm.addAlarmAction(Ec2Action(Ec2InstanceAction.RECOVER))

        val patchWindow = CfnMaintenanceWindow.Builder.create(this, "PatchMaintenanceWindow")
            .name("tractor-store-weekly-patch")
            .description("Weekly OS patching via AWS-RunPatchBaseline")
            .schedule("cron(0 4 ? * SUN *)")
            .scheduleTimezone("UTC")
            .duration(2)
            .cutoff(1)
            .allowUnassociatedTargets(false)
            .build()

        val patchTarget = CfnMaintenanceWindowTarget.Builder.create(this, "PatchTarget")
            .windowId(patchWindow.ref)
            .name("tractor-store-app-instance")
            .resourceType("INSTANCE")
            .targets(
                listOf(
                    CfnMaintenanceWindowTarget.TargetsProperty.builder()
                        .key("InstanceIds")
                        .values(listOf(instance.instanceId))
                        .build(),
                ),
            )
            .build()

        CfnMaintenanceWindowTask.Builder.create(this, "PatchTask")
            .windowId(patchWindow.ref)
            .name("tractor-store-app-patch-task")
            .description("Install pending patches + reboot if required")
            .taskType("RUN_COMMAND")
            .taskArn("AWS-RunPatchBaseline")
            .priority(1)
            .maxConcurrency("1")
            .maxErrors("1")
            .targets(
                listOf(
                    CfnMaintenanceWindowTask.TargetProperty.builder()
                        .key("WindowTargetIds")
                        .values(listOf(patchTarget.ref))
                        .build(),
                ),
            )
            .taskInvocationParameters(
                CfnMaintenanceWindowTask.TaskInvocationParametersProperty.builder()
                    .maintenanceWindowRunCommandParameters(
                        CfnMaintenanceWindowTask.MaintenanceWindowRunCommandParametersProperty.builder()
                            .parameters(
                                mapOf(
                                    "Operation" to listOf("Install"),
                                    "RebootOption" to listOf("RebootIfNeeded"),
                                ),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

        CfnOutput.Builder.create(this, "Ec2InstanceId")
            .value(instance.instanceId)
            .description("Use this ID for `aws ssm send-command` in the deploy workflow.")
            .build()
        CfnOutput.Builder.create(this, "Ec2PublicIp")
            .value(eip.ref)
            .description("Point the tractorstore.inauditech.com A record at this address.")
            .build()
        CfnOutput.Builder.create(this, "Ec2LogGroupName")
            .value(logGroup.logGroupName)
            .build()
    }

    private fun renderBootstrapScript(): String {
        val ecrRegistry = "${Config.ACCOUNT}.dkr.ecr.${Config.REGION}.amazonaws.com"
        val ecrRepoUri = "$ecrRegistry/${Config.ECR_REPO_NAME}"
        val deployScript = loadResource("userdata/deploy.sh")
        return loadResource("userdata/bootstrap.sh")
            .replace("%REGION%", Config.REGION)
            .replace("%DOMAIN%", Config.DOMAIN_NAME)
            .replace("%APP_PORT%", Config.APP_PORT.toString())
            .replace("%ECR_REGISTRY%", ecrRegistry)
            .replace("%ECR_REPO_URI%", ecrRepoUri)
            .replace("%LOG_GROUP%", Config.EC2_LOG_GROUP_NAME)
            .replace("%DEPLOY_SCRIPT%", deployScript.trimEnd())
    }

    private fun loadResource(path: String): String =
        this::class.java.classLoader.getResource(path)?.readText()
            ?: error("Missing classpath resource: $path")
}
