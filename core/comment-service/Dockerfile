FROM amazoncorretto:21-alpine
LABEL authors="Dmitriy Gorokhov"
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
