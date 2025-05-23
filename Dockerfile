# syntax=docker/dockerfile:1
FROM gradle:8.14.0-jdk21 AS build
WORKDIR /app

COPY . .
RUN gradle --no-daemon --no-parallel --console=plain clean build

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user for running the app
RUN useradd -m appuser

# Copy only the built jar
COPY --from=build /app/build/libs/*.jar /app/app.jar
COPY --from=build /app/src/main/resources/ /app/resources/

ENV JAVA_OPTS=""
ENV SMPP_SIM_PORT=2775
ENV SMPP_SIM_USERS_FILE=users.txt

USER appuser

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD pgrep java || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $SMPP_SIM_PORT $SMPP_SIM_USERS_FILE"]
