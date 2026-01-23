#!/bin/bash

# QQ机器人应用启动脚本
# 用于在Linux环境下运行Spring Boot应用

# 应用名称
APP_NAME="qqboot"
JAR_FILE="qqboot-1.0-SNAPSHOT.jar"

# Java参数配置
JAVA_OPTS="-server -Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 应用参数配置
APP_OPTS="--spring.profiles.active=prod"

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 找不到JAR文件 $JAR_FILE"
    exit 1
fi

# 检查Java是否已安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java运行时环境"
    exit 1
fi

echo "正在启动 $APP_NAME..."

# 启动应用
java $JAVA_OPTS -jar $JAR_FILE $APP_OPTS

echo "$APP_NAME 已停止"