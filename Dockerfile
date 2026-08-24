# ================================
# Stage 1: Build (Gradle)
# ================================
# Gradle wrapper JAR repoda yo'q (README'da yozilgan) — shuning uchun
# wrapper generatsiya qilib o'tirmasdan, rasmiy gradle image'idan foydalanamiz.
FROM gradle:8.12-jdk21 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src

RUN --mount=type=cache,target=/root/.gradle gradle clean build -x test --no-daemon

# ================================
# Stage 2: Run
# ================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S finflow && adduser -S finflow -G finflow
USER finflow

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]