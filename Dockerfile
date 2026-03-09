# ========== Stage 1: Build ==========
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# cache dependencies
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

# copy source
COPY . .

RUN gradle clean bootJar --no-daemon

# ========== Stage 2: Runtime ==========
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Ho_Chi_Minh

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]