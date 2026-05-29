# Migrate tractor-store from App Runner to EC2 (CDK, account-shared only)

## Context

`tractor-store-htmx-tailwind` runs on App Runner: a single Docker image with NGINX (port 3000) plus two Spring Boot apps (`discover` 8080, `checkout` 8081), built and pushed to ECR (`tractor-store-htmx-tailwind`, `eu-central-1`) by `.github/workflows/build-and-deploy.yml`. AWS auth uses long-lived access keys.

Goal: replicate the cryptoanalytics App-Runner→EC2 migration — single cheap EC2 instance, Caddy fronting the container for TLS via Let's Encrypt, deploys via GitHub Actions + SSM `send-command`. The migration is finalized when the external DNS A record for `tractorstore.inauditech.com` is repointed at the new Elastic IP.

Independence requirement: tractor-store and cryptoanalytics share the AWS account only. No cross-stack references, no cross-repo PRs to operate one app or the other. The only resources that must be referenced (not duplicated) are the two genuinely account-level singletons that AWS itself enforces: the CDK bootstrap stack (`hnb659fds`) and the GitHub OIDC provider (`token.actions.githubusercontent.com`). Both are referenced by stable, well-known ARNs — no CDK lookup against the other repo's state.

## What is already in place

A CDK app under `infra/` is essentially complete and faithfully implements the "minimum dependency" model. Verified against the goal:

- `infra/cdk.json`, `infra/build.gradle.kts`, `infra/settings.gradle.kts`, `infra/gradlew` — self-contained Gradle 8.5 / Kotlin 1.9.24 / CDK 2.170.0 project. Distinct package `com.inauditech.tractorstore.infra`.
- `infra/src/main/kotlin/com/inauditech/tractorstore/infra/Config.kt` — `PROJECT = "tractor-store"`, ECR repo name matches the GH workflow, account ID resolved at synth time from `CDK_DEFAULT_ACCOUNT` / `AWS_ACCOUNT_ID` (never committed).
- `IamStack.kt:27-32` — references the existing OIDC provider via `OpenIdConnectProvider.fromOpenIdConnectProviderArn(this, "GitHubOidcProvider", "arn:aws:iam::${Config.ACCOUNT}:oidc-provider/token.actions.githubusercontent.com")`. Pure ARN reference — no cross-stack import.
- `IamStack.kt` — own `tractor-store-gha-deploy` role; trust subject scoped to this repo only; permissions scoped to this app's ECR ARN, account/region-wide EC2 instance wildcard for `ssm:SendCommand` (avoids cross-stack export), and the shared CDK bootstrap roles for the single region.
- `RegistryStack.kt` — own ECR repo with 8-image lifecycle + AES-256 + `RemovalPolicy.RETAIN`.
- `Ec2AppStack.kt` — default VPC lookup, own SG (80/443 in), own `tractor-store-app-ec2-instance-role` (only ECR pull + CloudWatch Logs + SSM core — no DynamoDB/S3/Cognito), AL2023 ARM `t4g.small`, 20 GB GP3 encrypted EBS, IMDSv2 required, Elastic IP, `SystemStatusCheckAlarm` with `Ec2InstanceAction.RECOVER`, weekly SSM patch maintenance window. Stack outputs: `Ec2InstanceId`, `Ec2PublicIp`, `Ec2LogGroupName`.
- `bootstrap.sh` — installs Docker, Caddy (binary tarball, deterministic version), single `app.service` systemd unit binding container `3000` to host loopback only; `dnf-automatic` for daily security updates; kpatch best-effort.
- `deploy.sh` — pointing the unit at the new image, restarting, health-probing `127.0.0.1:3000` and the public HTTPS endpoint, no blue/green.
- Stack names prefixed `TractorStore-*` so they cannot collide with cryptoanalytics stacks in the same account+region.

The CDK side aligns with the minimum-dependency intent — no edits needed in cryptoanalytics, no cross-stack exports.

## Resolved divergences

Two issues between the written CDK and the choices made earlier that need reconciling before deploy:

1. **CPU architecture (Keep ARM, fix the build).** `Ec2AppStack.kt:125,134` use `ARM_64` / `BURSTABLE4_GRAVITON`; `bootstrap.sh:42` downloads `caddy_…_linux_arm64.tar.gz`. The current Docker build is single-arch amd64 — wrong arch for a t4g host. Fix on the **build** side, not the CDK side: switch `build-and-deploy.yml` to buildx and produce a single-arch `linux/arm64` image (no need for multi-arch — only the EC2 host consumes it).
2. **ECR repo already exists.** `RegistryStack.kt:14-15` will fail on first `cdk deploy` with `RepositoryAlreadyExistsException`. Resolve by performing a one-time `cdk import` of the existing repo into `TractorStore-Registry-dev` before the first regular `cdk deploy`. After import, CDK owns lifecycle/encryption/tags going forward.

## Remaining work

### A. Rewrite `.github/workflows/build-and-deploy.yml` (single file edit)

Replace static keys + amd64 build + no-deploy with OIDC + arm64 build + SSM deploy. Steps:

- Remove the `Configure AWS credentials` step that uses `secrets.AWS_ACCESS_KEY_ID` / `secrets.AWS_SECRET_ACCESS_KEY`. Replace with `aws-actions/configure-aws-credentials@v4` using `role-to-assume: arn:aws:iam::<ACCOUNT>:role/tractor-store-gha-deploy`, `aws-region: eu-central-1`. Permissions already include `id-token: write`.
- Add `docker/setup-qemu-action@v3` and `docker/setup-buildx-action@v3` steps.
- Replace the manual `docker build/push` block with `docker/build-push-action@v6`:
  - `context: .`, `file: ./Dockerfile`, `platforms: linux/arm64`, `push: true`, `provenance: false`.
  - `tags`: `${{ steps.login-ecr.outputs.registry }}/${{ env.ECR_REPOSITORY }}:${{ github.sha }}` and `…:latest`.
- Export the image URI to a job output (e.g., `image-outputs.outputs.image`) so the deploy job can consume it.
- Add a second job `deploy` (`needs: build`) mirroring cryptoanalytics' `.github/workflows/deploy-ec2.yml` deploy job:
  1. OIDC step (same role).
  2. Resolve instance ID via `aws cloudformation describe-stacks --stack-name TractorStore-Ec2App-dev --query "Stacks[0].Outputs[?OutputKey=='Ec2InstanceId'].OutputValue"`.
  3. Base64-encode `infra/src/main/resources/userdata/deploy.sh` and ship it via `aws ssm send-command --document-name AWS-RunShellScript`, then invoke `/opt/app/deploy.sh <IMAGE_URI>`. Shipping deploy.sh fresh each run avoids stale-script drift, matching cryptoanalytics.
  4. Poll `aws ssm get-command-invocation` until `Success` / `Failed` / `TimedOut`. Fail the job on non-`Success`.

After cutover, delete the `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` repo secrets and the IAM user (if any) that backed them.

### B. Add `.github/workflows/infra-deploy.yml`

Copy from `cryptoanalytics/.github/workflows/infra-deploy.yml`, drop the Cognito post-deploy step. Trigger on `paths: ['infra/**', '.github/workflows/infra-deploy.yml']`. `cdk diff` on PR, `cdk deploy --all` on push to `main`. Uses the same `tractor-store-gha-deploy` OIDC role (which `IamStack.kt` already grants `sts:AssumeRole` on the bootstrap roles + `ssm:GetParameter` on the bootstrap version SSM param).

### C. First-time bring-up (one-time, manual, in this order)

1. **Build infra locally** to catch any compile errors: `cd infra && ./gradlew build`.
2. **Synth and inspect**: with admin/deployer creds active (`CDK_DEFAULT_ACCOUNT` set automatically by the cdk CLI), `cd infra && cdk synth --all`.
3. **Adopt the existing ECR repo** via `cdk import`:
   ```
   cd infra
   cdk import TractorStore-Registry-dev
   ```
   When prompted for the `AWS::ECR::Repository` physical id, supply `tractor-store-htmx-tailwind`. This adopts the existing repo without recreating it.
4. **Deploy the remaining stacks**: `cdk deploy --all`. Order is handled by `IamStack.addDependency(RegistryStack)` already declared in `App.kt:28`.
5. Capture the `Ec2PublicIp` output from `TractorStore-Ec2App-dev`. The instance will boot, install Docker + Caddy + the systemd unit; `app.service` retries pulling `:latest` until the first image is pushed in step 6. That retry loop is expected and harmless.
6. **Push the workflow changes** from §A and §B to `main`. The first run builds + pushes the arm64 image, then SSM-deploys to the new host. `journalctl -u app.service -f` (over `aws ssm start-session`) shows the container starting both Spring apps and NGINX. Caddy provisions a Let's Encrypt cert on first inbound request to port 80 — possible right now because the public IP is reachable, even though DNS still points at App Runner. (Caddy ACME HTTP-01 challenge needs the cert's DNS name to resolve to the host: until the DNS swap, Caddy will keep retrying. That is fine — the cert lands the moment DNS resolves.)
7. **Verify** before the DNS swap with a Host-header-pinned curl:
   `curl --resolve tractorstore.inauditech.com:443:<EIP> https://tractorstore.inauditech.com/` (with `-k` if cert hasn't issued yet) plus the same against `/product/...` (discover, 8080 inside container) and `/checkout/...` (checkout, 8081 inside container). This proves NGINX routing inside the container is intact.
8. **DNS cutover** at your external DNS provider: change the `A` record for `tractorstore.inauditech.com` from the App Runner target to the EIP. Watch for cert issuance via `journalctl -u caddy -f`. Verify in browser.
9. **Decommission** the App Runner service. Delete the `AWS_*` GitHub repo secrets and the IAM user that backed them.

Rollback: revert the DNS A record. App Runner stays up until step 9.

## Critical files

Already written (verify and leave as-is unless the divergences above require touching them):
- `infra/cdk.json`, `infra/build.gradle.kts`, `infra/settings.gradle.kts`
- `infra/src/main/kotlin/com/inauditech/tractorstore/infra/{App,Config,IamStack,RegistryStack,Ec2AppStack}.kt`
- `infra/src/main/resources/userdata/{bootstrap.sh,deploy.sh}`

To be added / edited in this work:
- **Edit**: `.github/workflows/build-and-deploy.yml` — OIDC, arm64 buildx, SSM deploy job.
- **Add**: `.github/workflows/infra-deploy.yml` — `cdk diff`/`cdk deploy` driver.

No changes to: `Dockerfile`, `entrypoint.sh`, `integration/nginx/*`, Spring app code, or anything in cryptoanalytics.

## Reused existing patterns

From cryptoanalytics (copied verbatim or near-verbatim where applicable):
- The `Resolve EC2 instance ID from CFN output` + `Send blue/green deploy command via SSM` + `Poll SSM command until done` job steps in `.github/workflows/deploy-ec2.yml` — drop the blue/green title, keep the ship-script-base64 pattern.
- The `infra-deploy.yml` shell (jdk17, node22, install CDK CLI, OIDC, `cdk diff`/`cdk deploy --all`) — drop `sync-hosted-ui.sh`.
- The Caddy systemd unit, dnf-automatic config, and patch maintenance window — already mirrored in this repo's `bootstrap.sh` and `Ec2AppStack.kt`.

## Verification

End-to-end checklist:

- `cd infra && cdk synth --all` succeeds; produces three templates (`TractorStore-Registry-dev`, `TractorStore-Iam-dev`, `TractorStore-Ec2App-dev`).
- After `cdk import` + `cdk deploy --all`: AWS console shows the EC2 instance Running, status checks Pass; the existing ECR repo now lists `RegistryStack` as its CFN owner.
- `aws ssm start-session --target <instance-id>` then `journalctl -u app.service` shows the container's `entrypoint.sh` starting `app-discover.jar`, `app-checkout.jar`, then NGINX.
- A push to `main`: `build-and-deploy.yml` builds an arm64 image, SSM deploy job reports `Success`, image SHA visible in `journalctl -u app.service`.
- Pre-DNS: `curl --resolve tractorstore.inauditech.com:443:<EIP> https://tractorstore.inauditech.com/` returns 200 (use `-k` if cert is still pending); `/product/...` and `/checkout/...` paths return content from the right Spring app.
- Post-DNS: `dig tractorstore.inauditech.com +short` returns the EIP; browser session works with a valid Let's Encrypt cert; CloudWatch Logs group `/ec2/tractor-store-app` receives steady-state requests.

## Cost note

One `t4g.small` (~$12/mo) + Elastic IP ($0 while attached) + 20 GB GP3 EBS + minor CloudWatch Logs. Independent of cryptoanalytics' bill.
