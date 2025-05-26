# Use a base image with Java 21
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Accept the JAR file name as a build argument
ARG JAR_FILE

# Copy the fat JAR file to the container, using the build argument
COPY ${JAR_FILE} app.jar

# Expose the port your app listens on
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
