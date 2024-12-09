#!/bin/sh

ENVIRONMENT="prod"

# Start the Spring Boot applications
java -jar -Dspring.profiles.active=$ENVIRONMENT /app-discover.jar &
# java -jar -Dspring.profiles.active=$ENVIRONMENT /app-checkout.jar &

# Start NGINX
echo "Start NGINX (nginx-$ENVIRONMENT.conf)..."
nginx -c /etc/nginx/nginx-$ENVIRONMENT.conf -g "daemon off;"