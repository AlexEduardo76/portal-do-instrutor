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

RUN useradd --system --uid 1001 --create-home app
WORKDIR /app

COPY --from=build --chown=app:app /app/backend/build/libs/*.jar app.jar

USER app

EXPOSE 8080

# Exec form: o java é o PID 1 e recebe SIGTERM no docker stop.
ENTRYPOINT ["java", "-jar", "app.jar"]
