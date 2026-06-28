# Étape 1 — Construction du jar
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le cache Maven local
COPY .m2 /root/.m2

COPY pom.xml .
COPY src ./src

# -o = mode offline, utilise uniquement le cache local
RUN mvn clean package -DskipTests -o

# Étape 2 — Image finale
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]