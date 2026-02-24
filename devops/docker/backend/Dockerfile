# ============================================
# IoT Platform - 后端 Dockerfile (多阶段构建)
# ============================================

# ---- 阶段1: Maven 编译 ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- 阶段2: JDK 运行 ----
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="iot-platform-devops"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/*.jar app.jar

ENV SERVER_PORT=8080
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

USER appuser

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
