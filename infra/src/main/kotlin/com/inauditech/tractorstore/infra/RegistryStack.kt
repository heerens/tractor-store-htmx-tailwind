package com.inauditech.tractorstore.infra

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ecr.LifecycleRule
import software.amazon.awscdk.services.ecr.Repository
import software.amazon.awscdk.services.ecr.RepositoryEncryption
import software.amazon.awscdk.services.ecr.TagMutability
import software.constructs.Construct

class RegistryStack(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {

    val repository: Repository = Repository.Builder.create(this, "TractorStoreRepo")
        .repositoryName(Config.ECR_REPO_NAME)
        .imageTagMutability(TagMutability.MUTABLE)
        .imageScanOnPush(false)
        .encryption(RepositoryEncryption.AES_256)
        .removalPolicy(RemovalPolicy.RETAIN)
        .lifecycleRules(
            listOf(
                LifecycleRule.builder()
                    .description("Keep only the 8 most recently pushed images")
                    .maxImageCount(8)
                    .rulePriority(1)
                    .build(),
            ),
        )
        .build()
}
