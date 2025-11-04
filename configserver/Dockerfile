# ==== Build ====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the parent and module POMs and sources
COPY pom.xml ./
COPY configserver/pom.xml configserver/pom.xml
COPY configserver/src configserver/src

RUN mvn -q -DskipTests -pl configserver -am package

# ==== Run ====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/configserver/target/*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]