# Use Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Run jar
ENTRYPOINT ["java","-jar","app.jar"]