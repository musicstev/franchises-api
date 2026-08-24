# ---------- Etapa de construcción ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cachear dependencias antes de copiar el código fuente
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY lombok.config .
COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Etapa de ejecución ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/target/franchises-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
