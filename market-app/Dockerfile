FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY market-app/pom.xml market-app/
COPY market-app/src market-app/src
RUN mvn -pl market-app clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system appuser
COPY --from=build /app/market-app/target/market-app-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
