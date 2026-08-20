# Build stage: compiles the app with Maven, isolated from the runtime image
# so the final image doesn't carry the JDK or the Maven cache around.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Downloads dependencies as their own layer, so a later `docker compose build`
# only re-downloads them if pom.xml itself changed, not on every source edit.
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage: just the JRE, curl (for docker-compose's app healthcheck
# against /actuator/health - not present in the base image by default),
# and the built jar.
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]