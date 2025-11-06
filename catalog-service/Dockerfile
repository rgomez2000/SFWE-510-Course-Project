FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY catalog-service/src catalog-service/src
RUN mvn -q -DskipTests -f catalog-service/pom.xml package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/catalog-service/target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
