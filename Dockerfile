# Use a base image with Java 17.
# Using Eclipse Temurin as it's a good OpenJDK distribution.
# Choose a slim image to reduce size.
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the application JAR
# Use -DskipTests to speed up image build; ensure tests pass in your CI/CD pipeline
RUN ./mvnw package -DskipTests

# --- Second Stage: Create the actual runtime image ---
# Use a JRE image as it's smaller than a JDK image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the JAR from the builder stage
# Your JAR file will be in target/your-app-name-version.jar. Adjust if needed.
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on (Spring Boot default is 8080)
# Cloud Run will override this with the PORT environment variable.
EXPOSE 8080

# Command to run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]
# Using exec form is generally preferred for signal handling:
CMD ["java", "-jar", "app.jar"]