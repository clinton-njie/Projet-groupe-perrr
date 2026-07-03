FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# On copie tout d'un coup
COPY pom.xml .
COPY src ./src

# On utilise le cache natif de Docker pour conserver les fichiers téléchargés
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]