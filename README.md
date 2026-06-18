# 校园互助平台 (PE-Student)

一个面向高校学生的移动端互助平台，旨在促进校园内的信息共享、资源互助与社交互动。平台以"互助、共享、连接"为核心理念，打造温暖、年轻、有活力的校园社区。

## 项目简介

校园互助平台提供以下核心功能：

- **帖子发布**：文字/图片发帖、话题标签、分类选择
- **二手交易**：商品发布、价格展示、交易状态、分类筛选
- **互动系统**：点赞、评论、收藏
- **私信系统**：一对一聊天、消息列表
- **用户中心**：个人资料、我的帖子、我的交易、收藏管理

## 技术架构

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5+ | 前端框架 |
| Vite | 8.0+ | 构建工具 |
| Vue Router | 4.6+ | 路由管理 |
| Pinia | 3.0+ | 状态管理 |
| Axios | 1.17+ | HTTP请求 |
| GSAP | 3.15+ | 动画引擎 |
| Tailwind CSS | 3.4+ | 原子化CSS |
| Vant | 4.9+ | 移动端UI组件库 |

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 后端框架 |
| Spring Security | 6.2+ | 安全认证 |
| Spring WebSocket | 6.1+ | 实时通信 |
| MyBatis Plus | 3.5+ | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存/会话 |
| JWT | 0.12+ | Token认证 |
| Aliyun OSS | 3.17+ | 文件存储 |
| Lombok | 1.18+ | 代码简化 |
| Hutool | 5.8+ | 工具库 |

### 项目结构

```
PE-Student/
├── student-app/              # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/              # API 接口封装
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── composables/      # 组合式函数
│   │   ├── layouts/          # 布局组件
│   │   ├── pages/            # 页面组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   └── utils/            # 工具函数
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
│
├── student-server/           # 后端项目 (Spring Boot)
│   ├── src/main/java/com/student/
│   │   ├── config/           # 配置类 (Security/WebSocket/CORS/Redis等)
│   │   ├── controller/       # 控制器层 (REST API)
│   │   ├── service/          # 服务层 (业务逻辑)
│   │   ├── mapper/           # 数据访问层 (MyBatis Plus)
│   │   ├── entity/           # 实体类 / DTO
│   │   ├── vo/               # 视图对象 (返回封装)
│   │   ├── enums/            # 枚举类
│   │   ├── exception/        # 异常处理
│   │   ├── utils/            # 工具类 (JWT/Security)
│   │   ├── interceptor/      # 拦截器 (限流)
│   │   └── StudentApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml   # 主配置文件
│   │   ├── application-dev.yml
│   │   ├── mapper/           # XML 映射文件
│   │   └── static/           # 前端静态资源
│   └── pom.xml
│
├── pe-student/               # 预留模块
├── LICENSE
└── README.md
```

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+ 或 pnpm 8+
- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+ (可选)

---

### 前端启动

#### 安装依赖

```bash
cd student-app
npm install
```

#### 开发模式

```bash
npm run dev
```

#### 生产构建

```bash
npm run build
```

#### 预览生产构建

```bash
npm run preview
```

---

### 后端启动

#### 1. 创建数据库

```sql
CREATE DATABASE student_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 配置数据库

编辑 `student-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

#### 3. 编译运行

```bash
cd student-server
mvn clean package
mvn spring-boot:run
```

或使用 IDEA 直接运行 `StudentApplication.java`。

后端服务默认运行在 `http://localhost:8080`。

---

### API 接口

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/auth/**` | 登录/注册/刷新Token |
| 用户 | `/api/user/**` | 用户信息/关注 |
| 帖子 | `/api/post/**` | 帖子发布/点赞/收藏 |
| 评论 | `/api/comment/**` | 评论发布/删除 |
| 交易 | `/api/trade/**` | 二手商品发布/收藏 |
| 消息 | `/api/message/**` | 私信/会话列表 |
| 通知 | `/api/notification/**` | 系统通知 |
| 上传 | `/api/upload/**` | 图片上传 |

WebSocket 端点：`/ws`，使用 STOMP 协议。

## 功能模块

### 已实现功能

- [x] 用户认证（登录/注册）
- [x] 首页信息流
- [x] 帖子发布与管理
- [x] 二手交易市场
- [x] 商品发布与管理
- [x] 评论系统
- [x] 点赞与收藏
- [x] 私信聊天
- [x] 消息通知
- [x] 个人中心
- [x] 搜索功能

### 页面列表

| 页面 | 路径 | 描述 |
|------|------|------|
| 首页 | `/` | 信息流、推荐内容 |
| 搜索 | `/search` | 关键词搜索 |
| 帖子详情 | `/post/:id` | 帖子内容、评论列表 |
| 发布帖子 | `/post/create` | 创建新帖子 |
| 二手市场 | `/trade` | 商品列表 |
| 商品详情 | `/trade/:id` | 商品信息 |
| 发布商品 | `/trade/create` | 发布二手商品 |
| 消息列表 | `/message` | 会话列表 |
| 聊天页面 | `/message/:userId` | 私信聊天 |
| 个人中心 | `/user` | 用户信息 |

## 设计规范

### 色彩系统

- **主色调**：温暖珊瑚橙 `#f05a28`
- **辅助色**：清新薄荷绿 `#14b88a`
- **背景色**：深色主题 `#1a1c24`

### 字体

- **标题字体**：DM Serif Display
- **正文字体**：DM Sans
- **等宽字体**：JetBrains Mono

## 浏览器支持

- iOS Safari 12+
- Chrome 80+
- Firefox 75+
- Edge 80+

## 贡献指南

欢迎提交 Issue 和 Pull Request。

## 许可证

[MIT](LICENSE)

---

> 校园互助 — 连接每一份温暖
