FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml ./

COPY eurekaserver/pom.xml eurekaserver/pom.xml

COPY eurekaserver/src eurekaserver/src

RUN mvn -q -DskipTests -f eurekaserver/pom.xml package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/eurekaserver/target/*.jar app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
