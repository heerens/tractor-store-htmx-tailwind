#!/bin/bash
# CDK-rendered userdata for the tractor-store EC2 host.
# Placeholders (%FOO%) are substituted at synth time by Ec2AppStack.kt.
# Single-color deploy (no blue/green): a brief restart blip on deploy is acceptable.
set -euxo pipefail

REGION="%REGION%"
DOMAIN="%DOMAIN%"
APP_PORT="%APP_PORT%"
ECR_REGISTRY="%ECR_REGISTRY%"
ECR_REPO_URI="%ECR_REPO_URI%"
LOG_GROUP="%LOG_GROUP%"
INITIAL_IMAGE_TAG="latest"
CADDY_VERSION="2.8.4"

LOGFILE=/var/log/tractorstore-bootstrap.log
mkdir -p /var/log
exec > >(tee -a "$LOGFILE") 2>&1

echo "=== bootstrap.sh starting at $(date -Is) ==="

# ----- Base packages -----
# curl-minimal ships preinstalled on AL2023 and provides the curl command —
# do NOT install the full curl package; it conflicts with curl-minimal.
dnf install -y docker jq tar shadow-utils dnf-plugins-core
dnf install -y aws-cli || true  # usually preinstalled on AL2023

# ----- dnf-automatic: daily security updates, no reboot -----
dnf install -y dnf-automatic
sed -i 's/^apply_updates =.*/apply_updates = yes/' /etc/dnf/automatic.conf
sed -i 's/^upgrade_type =.*/upgrade_type = security/' /etc/dnf/automatic.conf
systemctl enable --now dnf-automatic.timer

# ----- kpatch: live kernel patching (best-effort; absence is non-fatal) -----
dnf install -y dnf-plugin-kpatch || true
dnf kpatch auto || true

# ----- Docker -----
systemctl enable --now docker

# ----- Caddy (binary tarball, deterministic version) -----
curl -fsSL "https://github.com/caddyserver/caddy/releases/download/v${CADDY_VERSION}/caddy_${CADDY_VERSION}_linux_arm64.tar.gz" -o /tmp/caddy.tar.gz
tar -xzf /tmp/caddy.tar.gz -C /tmp caddy
install -m 0755 /tmp/caddy /usr/bin/caddy
rm -f /tmp/caddy /tmp/caddy.tar.gz
useradd --system --home /var/lib/caddy --create-home --shell /usr/sbin/nologin caddy || true
mkdir -p /etc/caddy /var/lib/caddy /var/log/caddy
chown caddy:caddy /var/lib/caddy /var/log/caddy

cat > /etc/caddy/Caddyfile <<EOF
${DOMAIN} {
    reverse_proxy 127.0.0.1:${APP_PORT}
}
EOF

cat > /etc/systemd/system/caddy.service <<'UNIT'
[Unit]
Description=Caddy web server
Documentation=https://caddyserver.com/docs/
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
User=caddy
Group=caddy
ExecStart=/usr/bin/caddy run --environ --config /etc/caddy/Caddyfile
ExecReload=/usr/bin/caddy reload --config /etc/caddy/Caddyfile --force
TimeoutStopSec=5s
LimitNOFILE=1048576
PrivateTmp=true
ProtectSystem=full
AmbientCapabilities=CAP_NET_BIND_SERVICE

[Install]
WantedBy=multi-user.target
UNIT

# ----- App env files -----
mkdir -p /opt/app /etc/sysconfig

cat > /etc/sysconfig/tractorstore <<EOF
AWS_REGION=${REGION}
DOMAIN=${DOMAIN}
APP_PORT=${APP_PORT}
EOF

cat > /etc/sysconfig/tractorstore-image <<EOF
IMAGE_URI=${ECR_REPO_URI}:${INITIAL_IMAGE_TAG}
EOF

# ----- App systemd unit (single color) -----
# The container's NGINX integration layer listens on ${APP_PORT}; bind only to loopback so
# Caddy is the sole public entrypoint.
cat > /etc/systemd/system/app.service <<UNIT
[Unit]
Description=Tractor Store app
After=docker.service network-online.target
Requires=docker.service

[Service]
Type=simple
EnvironmentFile=/etc/sysconfig/tractorstore
EnvironmentFile=/etc/sysconfig/tractorstore-image
ExecStartPre=-/usr/bin/docker rm -f app
ExecStartPre=/bin/bash -c 'aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}'
ExecStartPre=/usr/bin/docker pull \${IMAGE_URI}
ExecStart=/usr/bin/docker run --rm --name app \\
    -p 127.0.0.1:${APP_PORT}:${APP_PORT} \\
    --env-file /etc/sysconfig/tractorstore \\
    --log-driver=awslogs \\
    --log-opt awslogs-region=${REGION} \\
    --log-opt awslogs-group=${LOG_GROUP} \\
    --log-opt awslogs-stream=app \\
    \${IMAGE_URI}
ExecStop=/usr/bin/docker stop -t 30 app
Restart=on-failure
RestartSec=10s
TimeoutStartSec=180
SuccessExitStatus=143 SIGTERM

[Install]
WantedBy=multi-user.target
UNIT

# ----- deploy.sh -----
cat > /opt/app/deploy.sh <<'DEPLOY'
%DEPLOY_SCRIPT%
DEPLOY
chmod +x /opt/app/deploy.sh

# ----- Start services -----
systemctl daemon-reload
systemctl enable caddy
systemctl start caddy
systemctl enable app.service
systemctl start app.service

echo "=== bootstrap.sh completed at $(date -Is) ==="
