# 多阶段构建 - 构建阶段
FROM --platform=$BUILDPLATFORM node:20-alpine AS builder

WORKDIR /app

# 复制 package.json 先安装依赖（利用 Docker 缓存）
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# 运行阶段 - 使用支持多平台的 nginx 镜像
FROM nginx:1.25-alpine

LABEL maintainer="petadopt"
LABEL description="Pet Adoption System Frontend Admin"

# 删除默认配置
RUN rm -rf /usr/share/nginx/html/*

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
