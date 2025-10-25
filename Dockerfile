# Stage 1: Build the JAR
FROM maven:3.9.2-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven config and source
COPY pom.xml .
COPY src ./src

# Build JAR (skip tests if you want faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/customerdemowithoutdb-0.0.1-SNAPSHOT.jar app.jar

# Expose port your app runs on
EXPOSE 9095

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]