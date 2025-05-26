# Use a base image with Java 21
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the fat JAR file to the container
COPY build/libs/RandomMovieApp-1.0-SNAPSHOT-all.jar app.jar

# Expose the port your app listens on
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
