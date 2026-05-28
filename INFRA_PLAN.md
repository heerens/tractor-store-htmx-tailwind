# Infrastructure Plan — tractor-store-htmx-tailwind

This app runs in the **same AWS account** as `cryptoanalytics` (the shared account in
`eu-central-1`) but is **fully independent**: its own repo, its own CDK app, its own
AWS resources, its own deploy pipeline. One AWS account can host any number of independent
CDK apps — the only discipline required is avoiding name collisions and sharing the handful of
genuinely account-level resources. This document records how that independence is achieved and
how to bring the stack up.

## How the two apps stay independent

| Dimension | cryptoanalytics | tractor-store | Independent? |
|---|---|---|---|
| Git repo | `heerens/cryptoanalytics` | `heerens/tractor-store-htmx-tailwind` | ✅ separate |
| CDK app | `cryptoanalytics/infra` | `tractor-store-htmx-tailwind/infra` | ✅ separate `cdk.json` + entrypoint |
| CFN stack names | `RegistryStack-dev`, `Ec2AppStack-dev`, `IamStack-dev`, … | `TractorStore-Registry-dev`, `TractorStore-Ec2App-dev`, `TractorStore-Iam-dev` | ✅ distinct (prefixed) |
| ECR repo | `crypto-analytics` | `tractor-store-htmx-tailwind` | ✅ separate |
| EC2 instance / EIP | `crypto-analytics-app` (`t4g.small`) | `tractor-store-app` (`t4g.small`) | ✅ separate host |
| IAM roles | `crypto-analytics-*` | `tractor-store-*` | ✅ separate |
| Log group | `/ec2/crypto-analytics-app` | `/ec2/tractor-store-app` | ✅ separate |
| Domain | `www.squarito.com` | `tractorstore.inauditech.com` | ✅ separate |
| State stores | DynamoDB + S3 + Cognito | none (self-contained demo) | ✅ no shared data |

### The two things they *do* share (intentionally)

These are account-level resources that **cannot** be duplicated, so the new stack references
the existing ones rather than recreating them:

1. **CDK bootstrap stack** (`CDKToolkit`, qualifier `hnb659fds`). Bootstrap is per
   account+region and is meant to be shared by every CDK app. No new bootstrap is needed —
   `cdk deploy` synthesizes against the existing one. `IamStack.kt` grants the deploy role the
   right to assume the shared `cdk-hnb659fds-*` roles.
2. **GitHub OIDC provider** (`token.actions.githubusercontent.com`). AWS allows exactly one
   OIDC provider per URL per account, and cryptoanalytics already created it. `IamStack.kt`
   uses `OpenIdConnectProvider.fromOpenIdConnectProviderArn(...)` to **reference** it — creating
   a second one would fail with "Provider with url ... already exists".

Everything else is namespaced under the `tractor-store` prefix in `infra/.../Config.kt`, so the
two apps' resources never touch. Deploying or destroying either stack has zero effect on the
other.

## Stacks in this CDK app (`infra/`)

- **`TractorStore-Registry-dev`** (`RegistryStack.kt`) — ECR repo `tractor-store-htmx-tailwind`
  (must match `ECR_REPOSITORY` in the build workflow), keeps the 8 most recent images.
- **`TractorStore-Ec2App-dev`** (`Ec2AppStack.kt`) — a single `t4g.small` (AL2023, ARM) in the
  default VPC's public subnet, with an Elastic IP, fronted by **Caddy** (auto Let's Encrypt TLS
  + reverse proxy). Includes a `StatusCheckFailed_System` auto-recovery alarm and a weekly SSM
  Patch Manager maintenance window (Sun 04:00 UTC). The instance role is minimal: ECR pull +
  CloudWatch Logs + SSM core (no DynamoDB/S3/Cognito — this app needs none).
- **`TractorStore-Iam-dev`** (`IamStack.kt`) — the GitHub Actions OIDC deploy role
  (`tractor-store-gha-deploy`): push to this app's ECR repo, run `ssm:SendCommand` to trigger a
  deploy, and assume the shared CDK bootstrap roles.

## Runtime model

The app ships as **one container** (see repo `Dockerfile`): two Spring Boot apps (`discover`,
`checkout`) plus an NGINX "integration" layer that composes them. NGINX listens on **port 3000**
— that is the single public port. On the EC2 host:

```
Internet ──443──> Caddy (TLS, tractorstore.inauditech.com) ──> 127.0.0.1:3000 ──> container :3000 (NGINX)
                                                                                      ├─ :8080 discover
                                                                                      └─ :8081 checkout
```

**No blue/green** (per requirement): `deploy.sh` repoints the systemd unit at the new image and
restarts it, with a health probe on `127.0.0.1:3000` and a public smoke test over HTTPS. There
is a brief (seconds) unavailability window during restart — acceptable for this demo.

## One-time bring-up

1. **Build the infra project** (self-contained Gradle project with its own wrapper, Gradle 8.5):
   ```bash
   cd infra
   ./gradlew build
   ```
2. **Install the AWS CDK CLI** if not already present: `npm i -g aws-cdk`.
3. **Bootstrap is already done** for this account+region (shared with cryptoanalytics). Nothing
   to do. (If you ever target a fresh account: `cdk bootstrap aws://<ACCOUNT_ID>/eu-central-1`.)
4. **Deploy the stacks** (from `infra/`, with admin/deployer credentials for the account):
   ```bash
   cdk deploy --all
   ```
   The account ID is resolved at synth time from `CDK_DEFAULT_ACCOUNT` (the cdk CLI sets this
   from your active credentials — nothing to configure). For a bare `./gradlew run` outside the
   cdk CLI, export `AWS_ACCOUNT_ID` first. The ID is never committed to this public repo.

   Order is handled by the dependency (`Iam` depends on `Registry`). On first deploy the EC2
   `userData` (`bootstrap.sh`) installs Docker + Caddy and starts the app from the `:latest`
   image — which won't exist until the first image is pushed (step 6), so the app stays in a
   retry loop until then. That's fine.
5. **Point DNS at the instance.** Take the `Ec2PublicIp` output from `TractorStore-Ec2App-dev`
   and set the external `tractorstore.inauditech.com` A record to it. Caddy issues a Let's
   Encrypt cert automatically once the record resolves. (You're migrating off the existing
   App Runner at the same hostname — cut DNS over once the new host is verified healthy.)
6. **Push the first image** by running the existing `build-and-deploy.yml` (or `docker build`
   + push manually). The instance picks up `:latest` on its next restart, or trigger a deploy
   (step below).

## Wiring up CI/CD (recommended changes to `build-and-deploy.yml`)

The current workflow uses **static AWS keys** (`secrets.AWS_ACCESS_KEY_ID` /
`AWS_SECRET_ACCESS_KEY`) and only builds + pushes to ECR — it does not deploy to a host. Two
recommended upgrades, both optional but cleaner:

1. **Switch to OIDC** (drops the static keys; uses the role `IamStack` creates). Replace the
   `Configure AWS credentials` step with:
   ```yaml
   - name: Configure AWS credentials
     uses: aws-actions/configure-aws-credentials@v4
     with:
       role-to-assume: arn:aws:iam::<ACCOUNT_ID>:role/tractor-store-gha-deploy
       aws-region: eu-central-1
   ```
   (The workflow already sets `permissions: id-token: write`.)
2. **Add a deploy step** after the image push to trigger the single-color deploy via SSM:
   ```yaml
   - name: Deploy to EC2
     run: |
       INSTANCE_ID=$(aws cloudformation describe-stacks \
         --stack-name TractorStore-Ec2App-dev \
         --query "Stacks[0].Outputs[?OutputKey=='Ec2InstanceId'].OutputValue" --output text)
       CMD_ID=$(aws ssm send-command \
         --instance-ids "$INSTANCE_ID" \
         --document-name AWS-RunShellScript \
         --parameters "commands=[\"/opt/app/deploy.sh ${{ steps.build-image.outputs.image }}\"]" \
         --query "Command.CommandId" --output text)
       aws ssm wait command-executed --command-id "$CMD_ID" --instance-id "$INSTANCE_ID"
   ```

An **infra deploy workflow** (mirroring cryptoanalytics' `infra-deploy.yml`: `cdk diff` on PR,
`cdk deploy` on merge to `main` via the same OIDC role) can be added later if you want infra
changes to ship from CI rather than locally.

## Layout

```
infra/
├── cdk.json                       # app = ./gradlew run; env=dev; default bootstrap qualifier
├── build.gradle.kts               # Kotlin 1.9.24, Java 17, aws-cdk-lib 2.170.0
├── settings.gradle.kts
├── gradlew + gradle/wrapper/       # Gradle 8.5 (copied from the discover submodule)
└── src/main/
    ├── kotlin/com/inauditech/tractorstore/infra/
    │   ├── App.kt                  # entrypoint; instantiates the 3 stacks (prefixed names)
    │   ├── Config.kt               # all names/IDs in one place
    │   ├── RegistryStack.kt        # ECR repo
    │   ├── Ec2AppStack.kt          # EC2 + EIP + Caddy + alarms + patch window
    │   └── IamStack.kt             # OIDC deploy role (references shared OIDC provider)
    └── resources/userdata/
        ├── bootstrap.sh            # cloud-init: Docker, Caddy, single app.service
        └── deploy.sh               # single-color restart + health/smoke probe
```

## Cost note

Adds roughly one `t4g.small` (~$12/mo) + an Elastic IP + EBS (20 GB gp3) + minor CloudWatch
Logs. Independent of the cryptoanalytics instance.
