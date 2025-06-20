FROM eclipse-temurin:21-jdk-alpine

WORKDIR /main

COPY build/libs/payments-service.jar app.jar
COPY src/main/resources/application.yml /main/application.yml

ENTRYPOINT ["java", "-jar", "app.jar"]