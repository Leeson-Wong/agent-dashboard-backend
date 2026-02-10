# 多阶段构建
FROM maven:3.9.12-eclipse-temurin-17 AS build

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建（跳过测试）
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 运行应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
