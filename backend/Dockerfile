# 多阶段构建 - 构建阶段
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 先下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段 - 使用支持多平台的基础镜像
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="petadopt"
LABEL description="Pet Adoption System Backend API"

WORKDIR /app

# 创建上传目录并设置权限
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
