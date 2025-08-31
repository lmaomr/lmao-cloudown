# Cloudown 云存储系统

## 项目简介
Cloudown 是一个基于 Spring Boot 和 Vue.js 的现代云存储系统。

## 技术栈

### 前端 (cloud-fronted)
- **框架：** Vue 3
- **构建工具：** Vite 6.2.4
- **UI组件库：** Element Plus 2.10.2
- **状态管理：** Pinia
- **路由管理：** Vue Router 4.5.0
- **HTTP客户端：** Axios
- **代码规范：** ESLint + Prettier

### 后端 (cloudown)
- **框架：** Spring Boot 3.5.3
- **JDK版本：** Java 17
- **构建工具：** Maven
- **日志系统：** 已集成（详细日志记录）

## 项目结构

```
├── cloud-fronted/          # 前端项目
│   ├── src/
│   │   ├── api/           # API请求
│   │   ├── components/    # Vue组件
│   │   ├── stores/        # Pinia状态管理
│   │   ├── router/        # 路由配置
│   │   ├── views/         # 页面视图
│   │   └── utils/         # 工具函数
│   └── ...
│
└── cloudown/              # 后端项目
    ├── src/
    │   ├── main/         # 主要代码
    │   └── test/         # 测试代码
    └── ...
```

## 开发指南

### 前端开发
```bash
# 进入前端项目目录
cd cloud-fronted

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 后端开发
```bash
# 进入后端项目目录
cd cloudown

# 使用 Maven 构建项目
./mvnw clean install

# 运行项目
./mvnw spring-boot:run
```

## 主要功能
- 文件管理
- 文件上传/下载
- 用户管理
- 路径管理
