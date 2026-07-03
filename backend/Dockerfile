FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copia o jar que eu já buildei direto no seu terminal em 6 segundos
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
