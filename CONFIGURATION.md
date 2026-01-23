# 项目配置说明

## 配置文件结构

本项目采用多环境配置文件管理方式，以确保敏感信息安全：

- [application.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application.yml) - 主配置文件，包含非敏感的公共配置
- [application-dev.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-dev.yml) - 开发环境配置，包含开发环境的敏感信息
- [application-prod.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-prod.yml) - 生产环境配置，使用环境变量管理敏感信息
- [application-example.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-example.yml) - 配置示例文件

## 环境激活

- 默认激活 `dev` 环境
- 可通过命令行参数指定环境：`--spring.profiles.active=prod`

## 安全建议

1. **不要将包含真实敏感信息的配置文件提交到版本控制系统**
2. **在生产环境中使用环境变量管理敏感信息**
3. **定期更换敏感信息（如API密钥、密码等）**

## 配置项说明

### 开发环境 ([application-dev.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-dev.yml))

包含以下敏感信息：
- QQ机器人密码
- DeepSeek API密钥
- 数据库用户名和密码

### 生产环境 ([application-prod.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-prod.yml))

使用环境变量管理敏感信息：
- `BOT_PASSWORD` - 机器人密码
- `BOT_LISTENER_GROUP` - 监听群组ID
- `DEEPSEEK_API_KEY` - DeepSeek API密钥
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `DB_URL` - 数据库连接URL

## 部署说明

### 本地开发

默认使用 `dev` 环境，会自动加载 [application-dev.yml](file:///E:/myGitRepositorysx/qqboot/src/main/resources/application-dev.yml) 配置。

### 生产部署

运行时应指定 `prod` 环境并设置相应的环境变量：

```bash
export BOT_PASSWORD=your_bot_password
export DEEPSEEK_API_KEY=your_deepseek_api_key
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
java -jar your-app.jar --spring.profiles.active=prod
```