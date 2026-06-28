FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/abonnement-service.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]