FROM openjdk:17-jdk-slim
ARG JAR_FILE=build/libs/common-library-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
