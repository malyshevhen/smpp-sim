FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar
COPY --from=build /app/src/main/resources/ /app/resources/

ENV JAVA_OPTS=""
ENV SMPP_SIM_PORT=2775
ENV SMPP_SIM_USERS_FILE=users.txt

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $SMPP_SIM_PORT $SMPP_SIM_USERS_FILE"]
