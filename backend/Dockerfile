# ============================================
# IoT Platform - 后端 Dockerfile (多阶段构建)
# ARM64 / AMD64 兼容
# ============================================

# ---- 阶段1: Maven 编译 ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- 阶段2: JDK 运行 ----
FROM eclipse-temurin:17-jdk

LABEL maintainer="iot-platform-devops"

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENV SERVER_PORT=8080
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
