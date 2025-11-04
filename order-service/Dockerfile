FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src
RUN mvn -q -DskipTests -pl order-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/order-service/target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]