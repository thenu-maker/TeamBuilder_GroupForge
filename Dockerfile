# Base-Image
FROM amazoncorretto:23-alpine-jdk

# Expose Port
EXPOSE 80

# Application jar-File
ARG JAR_FILE=target/*.jar

# Add jar to Container
ADD ${JAR_FILE} app.jar

# Start jar-File
ENTRYPOINT ["java", "-jar", "/app.jar"]