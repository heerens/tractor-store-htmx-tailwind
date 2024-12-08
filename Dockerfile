FROM amazoncorretto:22.0.2-alpine

# Install NGINX
RUN apk update && apk add --no-cache nginx

# Copy Spring apps

ADD discover/build/libs/app.jar /app-discover.jar
ADD checkout/build/libs/app.jar /app-checkout.jar

# Copy the default NGINX configuration file
COPY integration/nginx/nginx.conf /etc/nginx/nginx.conf
COPY integration/nginx/nginx-prod.conf /etc/nginx/nginx-prod.conf


COPY integration/nginx/html /usr/share/nginx/html
COPY integration/patternlib /usr/share/nginx/html
COPY integration/cdn /usr/share/nginx/html/cdn


# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose ports for the NGINX server and Spring Boot apps
EXPOSE 3000 8080 8081

# Set the entrypoint
ENTRYPOINT ["/entrypoint.sh"]