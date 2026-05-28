package com.inauditech.tractorstore.infra

object Config {
    // Same AWS account + region as the cryptoanalytics app. Everything below is namespaced
    // under the `tractor-store` PROJECT prefix so the two apps never collide on resource
    // names — see INFRA_PLAN.md for the full independence rationale.
    //
    // The account ID is read from the environment (CDK_DEFAULT_ACCOUNT, which the CDK CLI
    // populates from the active credentials) rather than hardcoded, so it never lands in this
    // public repo. Falls back to AWS_ACCOUNT_ID for non-CDK-CLI invocations (e.g. plain
    // `./gradlew run`). Both are resolved at synth time, not committed.
    val ACCOUNT: String = System.getenv("CDK_DEFAULT_ACCOUNT")
        ?: System.getenv("AWS_ACCOUNT_ID")
        ?: error("Set CDK_DEFAULT_ACCOUNT (auto-set by the cdk CLI) or AWS_ACCOUNT_ID")
    const val REGION = "eu-central-1"

    const val PROJECT = "tractor-store"

    // GitHub repo whose `main` branch is trusted by the OIDC deploy role.
    const val GITHUB_OWNER = "heerens"
    const val GITHUB_REPO = "tractor-store-htmx-tailwind"
    const val GITHUB_BRANCH = "main"

    // Public hostname. DNS is managed externally (not Route53): point a tractorstore A record
    // at the instance's Elastic IP (the Ec2PublicIp stack output). Caddy obtains a Let's Encrypt
    // cert for this name automatically once the A record resolves to the EIP.
    const val DOMAIN_NAME = "tractorstore.inauditech.com"

    // The container's NGINX "integration" layer serves the composed storefront on 3000;
    // the discover/checkout Spring Boot apps sit behind it on 8080/8081 (see entrypoint.sh).
    // Caddy reverse-proxies to this single public port.
    const val APP_PORT = 3000

    // Must match ECR_REPOSITORY in .github/workflows/build-and-deploy.yml.
    const val ECR_REPO_NAME = "tractor-store-htmx-tailwind"

    const val EC2_INSTANCE_NAME = "tractor-store-app"
    const val EC2_INSTANCE_ROLE_NAME = "tractor-store-app-ec2-instance-role"
    const val EC2_LOG_GROUP_NAME = "/ec2/tractor-store-app"

    const val GHA_DEPLOY_ROLE_NAME = "tractor-store-gha-deploy"
}
