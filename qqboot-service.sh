#!/bin/bash

# QQ机器人服务管理脚本
# 提供启动、停止、重启和状态检查功能

# 配置参数
APP_NAME="qqboot"
JAR_FILE="qqboot-1.0-SNAPSHOT.jar"
PID_FILE="/tmp/${APP_NAME}.pid"
LOG_FILE="./logs/${APP_NAME}.log"
JAVA_OPTS="-server -Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/"
APP_OPTS="--spring.profiles.active=prod"

# 创建日志目录
mkdir -p ./logs

# 检查JAR文件是否存在
check_jar_file() {
    if [ ! -f "$JAR_FILE" ]; then
        echo "错误: 找不到JAR文件 $JAR_FILE"
        exit 1
    fi
}

# 检查Java是否已安装
check_java() {
    if ! command -v java &> /dev/null; then
        echo "错误: 未找到Java运行时环境"
        exit 1
    fi
}

# 启动服务
start() {
    check_jar_file
    check_java
    
    # 检查服务是否已经在运行
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "$APP_NAME 已经在运行 (PID: $PID)"
            return 1
        else
            # PID文件存在但进程不在运行，删除PID文件
            rm -f $PID_FILE
        fi
    fi
    
    echo "正在启动 $APP_NAME..."
    
    # 启动应用并将输出重定向到日志文件
    nohup java $JAVA_OPTS -jar $JAR_FILE $APP_OPTS >> $LOG_FILE 2>&1 &
    PID=$!
    
    # 保存PID到文件
    echo $PID > $PID_FILE
    
    # 等待一段时间确保应用已启动
    sleep 3
    
    # 检查应用是否成功启动
    if ps -p $PID > /dev/null 2>&1; then
        echo "$APP_NAME 启动成功 (PID: $PID)"
        return 0
    else
        echo "$APP_NAME 启动失败"
        rm -f $PID_FILE
        return 1
    fi
}

# 停止服务
stop() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "正在停止 $APP_NAME (PID: $PID)..."
            kill $PID
            
            # 等待进程结束
            TIMEOUT=30
            while [ $TIMEOUT -gt 0 ] && ps -p $PID > /dev/null 2>&1; do
                sleep 1
                TIMEOUT=$((TIMEOUT - 1))
            done
            
            # 如果进程仍在运行，则强制杀死
            if ps -p $PID > /dev/null 2>&1; then
                echo "强制杀死进程 (PID: $PID)"
                kill -9 $PID
            fi
            
            # 删除PID文件
            rm -f $PID_FILE
            echo "$APP_NAME 已停止"
        else
            echo "$APP_NAME 未运行 (PID文件存在但进程不存在)"
            rm -f $PID_FILE
        fi
    else
        echo "$APP_NAME 未运行"
    fi
}

# 检查服务状态
status() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "$APP_NAME 正在运行 (PID: $PID)"
            return 0
        else
            echo "$APP_NAME 未运行 (PID文件存在但进程不存在)"
            rm -f $PID_FILE
            return 1
        fi
    else
        echo "$APP_NAME 未运行"
        return 1
    fi
}

# 重启服务
restart() {
    stop
    sleep 2
    start
}

# 查看实时日志
tail_log() {
    if [ -f $LOG_FILE ]; then
        tail -f $LOG_FILE
    else
        echo "日志文件不存在: $LOG_FILE"
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 {start|stop|restart|status|log|help}"
    echo ""
    echo "start   - 启动服务"
    echo "stop    - 停止服务"
    echo "restart - 重启服务"
    echo "status  - 检查服务状态"
    echo "log     - 查看实时日志"
    echo "help    - 显示此帮助信息"
}

# 主逻辑
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    log)
        tail_log
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "无效参数: $1"
        show_help
        exit 1
        ;;
esac

exit $?