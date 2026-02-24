# ============================================
# IoT Platform - 前端 Dockerfile (多阶段构建)
# ============================================

# ---- 阶段1: Node 构建 ----
FROM node:18-alpine AS builder

WORKDIR /build

COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build

# ---- 阶段2: Nginx 运行 ----
FROM nginx:1.25-alpine

LABEL maintainer="iot-platform-devops"

RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# nginx.conf 放在前端项目根目录
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html

ENV TZ=Asia/Shanghai

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
