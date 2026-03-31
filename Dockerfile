# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml from the backend folder
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY backend/src ./src
RUN mvn clean package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Standard Spring Boot port (will be overridden by PORT env var in application.yml)
EXPOSE 8080

# Run the application with explicit port mapping, entropy fix, and IPv4 preference
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:8080} -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
