# QQ机器人应用 - Linux部署包

## 项目概述

这是一个基于Spring Boot和Mirai框架的QQ机器人应用，集成了DeepSeek AI对话功能和成语查询功能。

## 部署文件清单

本部署包包含以下文件：

### 1. 应用程序
- `qqboot-1.0-SNAPSHOT.jar` - 主应用程序JAR包

### 2. 启动脚本
- `run.sh` - 简单的启动脚本
- `qqboot-service.sh` - 完整的服务管理脚本（支持启动/停止/重启/状态检查）

### 3. 容器化部署
- `Dockerfile` - Docker镜像构建文件
- `docker-compose.yml` - Docker Compose配置文件

### 4. 配置文件示例
- `example-prod-config.yml` - 生产环境配置示例文件

### 5. 文档
- `deploy-instructions.md` - 详细部署说明文档

## 快速部署指南

### 方式一：直接运行
```bash
# 1. 添加执行权限
chmod +x run.sh

# 2. 直接运行
./run.sh
```

### 方式二：服务管理脚本
```bash
# 1. 添加执行权限
chmod +x qqboot-service.sh

# 2. 启动服务
./qqboot-service.sh start

# 3. 检查状态
./qqboot-service.sh status
```

### 方式三：Docker部署
```bash
# 1. 构建并启动
docker-compose up -d

# 2. 查看日志
docker-compose logs -f
```

## 重要提醒

1. **安全配置**：生产环境中请勿将敏感信息（QQ账号、密码、API密钥等）直接写入配置文件
2. **环境变量**：推荐使用环境变量管理敏感信息
3. **数据库配置**：请根据实际环境更新数据库连接信息
4. **端口检查**：确保8088端口未被占用

## 详细部署说明

更多信息请参阅 `deploy-instructions.md` 文件。

mcp 地址：https://mcp.so