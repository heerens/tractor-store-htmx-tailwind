package com.inauditech.tractorstore.infra

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps

fun main() {
    val app = App()

    val envName = (app.node.tryGetContext("env") as? String) ?: "dev"
    val env = Environment.builder()
        .account(Config.ACCOUNT)
        .region(Config.REGION)
        .build()
    val stackProps = StackProps.builder().env(env).build()

    // Stack ids are prefixed "TractorStore-" so they never clash with the cryptoanalytics
    // stacks (RegistryStack-dev, Ec2AppStack-dev, IamStack-dev) that live in the SAME
    // account+region. CloudFormation stack names are unique per account+region, not per CDK app.
    val registry = RegistryStack(app, "TractorStore-Registry-$envName", stackProps)
    val ec2 = Ec2AppStack(app, "TractorStore-Ec2App-$envName", stackProps)
    val iam = IamStack(
        app,
        "TractorStore-Iam-$envName",
        stackProps,
        ecrRepoName = registry.repository.repositoryName,
    )
    iam.addDependency(registry)

    @Suppress("UNUSED_VARIABLE") val ignoredEc2 = ec2

    app.synth()
}
