#!/bin/bash
# Single-color deploy. Invoked on the instance via:
#   aws ssm send-command --document-name AWS-RunShellScript \
#       --parameters 'commands=["/opt/app/deploy.sh <IMAGE_URI>"]'
# Embedded into bootstrap.sh at synth time via %DEPLOY_SCRIPT% substitution.
#
# No blue/green: we point the unit at the new image and restart it. There is a short
# (seconds) window during container startup where the app is unavailable — acceptable for
# this demo. The app self-recovers via systemd Restart=on-failure if startup crashes.
set -euxo pipefail

IMAGE_URI="${1:?IMAGE_URI required as first argument}"

# shellcheck disable=SC1091
source /etc/sysconfig/tractorstore  # provides DOMAIN, AWS_REGION, APP_PORT

echo "[deploy.sh] Deploying ${IMAGE_URI}"
echo "IMAGE_URI=${IMAGE_URI}" > /etc/sysconfig/tractorstore-image

systemctl daemon-reload
systemctl restart app.service

# Health probe the app on its loopback port.
healthy=false
for i in $(seq 1 45); do
    if curl -fsS "http://127.0.0.1:${APP_PORT}/" > /dev/null 2>&1; then
        echo "[deploy.sh] App healthy after $i attempts"
        healthy=true
        break
    fi
    sleep 2
done

if [ "$healthy" != "true" ]; then
    echo "[deploy.sh] Health probe failed on 127.0.0.1:${APP_PORT}; check 'journalctl -u app.service'" >&2
    exit 1
fi

# Public smoke test through Caddy/TLS.
public_ok=false
for i in $(seq 1 5); do
    if curl -fsS "https://${DOMAIN}/" > /dev/null 2>&1; then
        echo "[deploy.sh] Public smoke test passed on attempt $i"
        public_ok=true
        break
    fi
    sleep 2
done

if [ "$public_ok" != "true" ]; then
    echo "[deploy.sh] Public smoke test failed (DNS/cert may still be propagating)" >&2
    exit 1
fi

echo "[deploy.sh] Deploy complete"
