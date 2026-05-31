# ---- Build stage ----
# JDK 21: Lombok 1.18.36 does not support JDK 25 (Maven's current default),
# so we pin the toolchain explicitly here.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Resolve dependencies first so this layer is cached unless pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Tests run against H2 in the regular `mvn verify`; the image build skips them
# to keep image builds fast and free of a DB dependency.
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/sms-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
