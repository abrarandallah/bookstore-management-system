# Build stage: compiles the app with Maven, isolated from the runtime image
# so the final image doesn't carry the JDK or the Maven cache around.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Downloads dependencies as their own layer, so a later `docker compose build`
# only re-downloads them if pom.xml itself changed, not on every source edit.
# The cache mount keeps Maven's download folder between builds on this machine,
# so even when pom.xml changes nothing is downloaded twice.
RUN --mount=type=cache,target=/root/.m2 mvn -q dependency:go-offline
COPY src ./src
# maven.test.skip also skips compiling the tests, which is quicker than -DskipTests.
RUN --mount=type=cache,target=/root/.m2 mvn -q package -Dmaven.test.skip=true

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