FROM gradle:8.14.0-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle --no-daemon --no-parallel --console=plain build

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/*.jar /app/app.jar
COPY --from=build /app/src/main/resources/ /app/resources/

ENV JAVA_OPTS=""
ENV SMPP_SIM_PORT=2775
ENV SMPP_SIM_USERS_FILE=users.txt

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $SMPP_SIM_PORT $SMPP_SIM_USERS_FILE"]
