# ---------- ETAPA 1: BUILD ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY backend/ ./backend/
COPY frontend/ ./frontend/

WORKDIR /app/backend

RUN chmod +x gradlew
RUN sed -i 's/\r$//' gradlew


ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=120000 -Dorg.gradle.internal.http.socketTimeout=120000"

RUN ./gradlew bootJar -x test --no-daemon

# ---------- ETAPA 2: RUNTIME ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]