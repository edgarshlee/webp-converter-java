FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sh mvnw --batch-mode dependency:go-offline
COPY src src
RUN sh mvnw --batch-mode package -DskipTests
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /workspace/target/webp-converter-java-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
