package com.inauditech.tractorstore.infra

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.IOpenIdConnectProvider
import software.amazon.awscdk.services.iam.OpenIdConnectProvider
import software.amazon.awscdk.services.iam.OpenIdConnectPrincipal
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.Role
import software.constructs.Construct

class IamStack(
    scope: Construct,
    id: String,
    props: StackProps,
    ecrRepoName: String,
) : Stack(scope, id, props) {

    // The GitHub OIDC provider is an ACCOUNT-LEVEL resource: there can be exactly one per URL
    // per account, and the cryptoanalytics IamStack already created it. We REFERENCE that
    // existing provider instead of creating a second one (which would fail with
    // "Provider with url ... already exists"). This is the canonical example of an account-wide
    // resource that two independent CDK apps must share — see INFRA_PLAN.md.
    private val githubOidcProvider: IOpenIdConnectProvider =
        OpenIdConnectProvider.fromOpenIdConnectProviderArn(
            this,
            "GitHubOidcProvider",
            "arn:aws:iam::${Config.ACCOUNT}:oidc-provider/token.actions.githubusercontent.com",
        )

    private val trustConditions: Map<String, Any> = mapOf(
        "StringEquals" to mapOf(
            "token.actions.githubusercontent.com:aud" to "sts.amazonaws.com",
        ),
        "StringLike" to mapOf(
            "token.actions.githubusercontent.com:sub" to
                "repo:${Config.GITHUB_OWNER}/${Config.GITHUB_REPO}:ref:refs/heads/${Config.GITHUB_BRANCH}",
        ),
    )

    val deployRole: Role = Role.Builder.create(this, "GhaDeployRole")
        .roleName(Config.GHA_DEPLOY_ROLE_NAME)
        .assumedBy(OpenIdConnectPrincipal(githubOidcProvider, trustConditions))
        .description("Assumed by GitHub Actions (main branch of ${Config.GITHUB_OWNER}/${Config.GITHUB_REPO}) for app + infra deploys.")
        .maxSessionDuration(Duration.hours(1))
        .build()

    init {
        val ecrRepoArn = "arn:aws:ecr:${Config.REGION}:${Config.ACCOUNT}:repository/$ecrRepoName"

        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("EcrAuth")
                .effect(Effect.ALLOW)
                .actions(listOf("ecr:GetAuthorizationToken"))
                .resources(listOf("*"))
                .build(),
        )
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("EcrPush")
                .effect(Effect.ALLOW)
                .actions(
                    listOf(
                        "ecr:BatchCheckLayerAvailability",
                        "ecr:InitiateLayerUpload",
                        "ecr:UploadLayerPart",
                        "ecr:CompleteLayerUpload",
                        "ecr:PutImage",
                        "ecr:BatchGetImage",
                        "ecr:DescribeImages",
                        "ecr:DescribeRepositories",
                    ),
                )
                .resources(listOf(ecrRepoArn))
                .build(),
        )

        // Account+region-wide on EC2 instances (not a specific instance ARN) so an AMI-driven
        // instance replacement in Ec2AppStack never creates a cross-stack Export this role
        // consumes — same rationale as the cryptoanalytics IamStack.
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("SsmSendCommand")
                .effect(Effect.ALLOW)
                .actions(listOf("ssm:SendCommand"))
                .resources(
                    listOf(
                        "arn:aws:ec2:${Config.REGION}:${Config.ACCOUNT}:instance/*",
                        "arn:aws:ssm:${Config.REGION}::document/AWS-RunShellScript",
                    ),
                )
                .build(),
        )
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("SsmCommandStatus")
                .effect(Effect.ALLOW)
                .actions(
                    listOf(
                        "ssm:GetCommandInvocation",
                        "ssm:ListCommandInvocations",
                        "ssm:ListCommands",
                    ),
                )
                .resources(listOf("*"))
                .build(),
        )
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("CfnDescribeStacks")
                .effect(Effect.ALLOW)
                .actions(listOf("cloudformation:DescribeStacks"))
                .resources(listOf("arn:aws:cloudformation:${Config.REGION}:${Config.ACCOUNT}:stack/*"))
                .build(),
        )

        // CDK bootstrap roles for the infra deploy. Shares the SAME default-qualifier (hnb659fds)
        // bootstrap stack as cryptoanalytics — the bootstrap is per account+region and is meant to
        // be shared across all CDK apps. Single region only (no us-east-1: this app has no
        // cross-region ACM/Cognito stack).
        val cdkRoles = listOf("deploy", "file-publishing", "image-publishing", "lookup").map {
            "arn:aws:iam::${Config.ACCOUNT}:role/cdk-hnb659fds-$it-role-${Config.ACCOUNT}-${Config.REGION}"
        }
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("AssumeCdkBootstrapRoles")
                .effect(Effect.ALLOW)
                .actions(listOf("sts:AssumeRole"))
                .resources(cdkRoles)
                .build(),
        )
        deployRole.addToPolicy(
            PolicyStatement.Builder.create()
                .sid("ReadCdkBootstrapVersion")
                .effect(Effect.ALLOW)
                .actions(listOf("ssm:GetParameter"))
                .resources(
                    listOf("arn:aws:ssm:${Config.REGION}:${Config.ACCOUNT}:parameter/cdk-bootstrap/hnb659fds/*"),
                )
                .build(),
        )

        CfnOutput.Builder.create(this, "DeployRoleArn")
            .value(deployRole.roleArn)
            .description("Use as `role-to-assume` in GitHub Actions workflows (OIDC).")
            .build()
    }
}
