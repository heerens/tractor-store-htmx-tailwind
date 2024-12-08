#Build stage

FROM gradle:latest AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle build

# Package stage

FROM amazoncorretto:22.0.2-alpine
ENV JAR_NAME=app.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
RUN ls -all
COPY --from=BUILD $APP_HOME .
RUN ls -all
EXPOSE 8081
ENTRYPOINT exec java -jar $APP_HOME/build/libs/$JAR_NAME

