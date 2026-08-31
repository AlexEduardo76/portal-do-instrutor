# ---------- ETAPA 1: BUILD ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew

ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=120000 -Dorg.gradle.internal.http.socketTimeout=120000"

RUN ./gradlew clean bootJar -x test --no-daemon

# ---------- ETAPA 2: RUNTIME ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]