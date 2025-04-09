# -------- Build stage --------
FROM maven:3.8.5-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Build the application, skipping tests
RUN mvn clean package -DskipTests


# -------- Runtime stage --------
FROM openjdk:17.0.1-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar JobPortal.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "JobPortal.jar"]
