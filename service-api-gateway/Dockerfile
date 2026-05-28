cat > service-api-gateway/Dockerfile << 'EOF'
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EOF