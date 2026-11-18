FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY market-app/pom.xml market-app/
COPY payment-service/pom.xml payment-service/.openapi-generator-java-sources.ignore payment-service/
COPY payment-service/src payment-service/src
RUN mvn -pl payment-service clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system appuser
COPY --from=build /app/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
