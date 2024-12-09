#!/bin/sh

ENVIRONMENT="prod"

# Trap SIGTERM and SIGINT signals to stop all processes cleanly
trap 'kill -TERM $PID_NGINX $PID_APP1 $PID_APP2; wait' TERM INT

# Start the Spring Boot applications
echo "Start Spring Boot apps (profile=${ENVIRONMENT})..."
java -jar -Dspring.profiles.active="${ENVIRONMENT}" /app-discover.jar &
PID_APP1=$!
java -jar -Dspring.profiles.active="${ENVIRONMENT}" /app-checkout.jar &
PID_APP2=$!

# Start NGINX in the background
echo "Start NGINX (nginx-$ENVIRONMENT.conf)..."
nginx -c /etc/nginx/nginx-$ENVIRONMENT.conf -g "daemon off;" &
PID_NGINX=$!

# Wait for all processes
wait