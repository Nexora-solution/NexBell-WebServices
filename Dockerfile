# ---- Build stage ----
# Java 24 + Maven (no hay maven wrapper en el repo, así que usamos la imagen oficial).
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Cache de dependencias: primero el pom, luego el código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render inyecta la variable PORT; application.properties usa server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
