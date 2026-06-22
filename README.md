# 校园互助平台 (PE-Student)

一个面向高校学生的移动端互助平台，旨在促进校园内的信息共享、资源互助与社交互动。平台以"互助、共享、连接"为核心理念，打造温暖、年轻、有活力的校园社区。

## 项目简介

校园互助平台提供以下核心功能：

- **帖子发布**：文字/图片发帖、话题标签、分类选择
- **二手交易**：商品发布、价格展示、交易状态、分类筛选
- **互动系统**：点赞、评论、收藏
- **私信系统**：一对一聊天、图片发送、表情包、消息列表
- **用户中心**：个人资料、我的帖子、我的交易、收藏管理
- **后台管理**：仪表盘统计、用户管理、内容审核、数据看板

## 技术架构

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5+ | 前端框架 |
| Vite | 6.0+ | 构建工具 |
| Vue Router | 4.5+ | 路由管理 |
| Pinia | 2.3+ | 状态管理 |
| Axios | 1.7+ | HTTP请求 |
| GSAP | 3.15+ | 动画引擎 |
| Tailwind CSS | 3.4+ | 原子化CSS |
| Vant | 4.9+ | 移动端UI组件库（学生端） |
| Element Plus | 2.9+ | 桌面端UI组件库（管理后台） |
| ECharts | 5.5+ | 数据图表（管理后台） |

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
├── student-app/              # 学生端前端 (Vue 3 + Vant)
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
│   │   ├── config/           # 配置类 (Security/WebSocket/CORS/Redis/Jackson等)
│   │   ├── controller/       # 控制器层
│   │   │   └── admin/        # 后台管理控制器
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
│   │   ├── schema.sql        # 数据库建表脚本
│   │   ├── seed-data.sql     # 演示数据
│   │   ├── mapper/           # XML 映射文件
│   │   └── static/           # 前端静态资源
│   └── pom.xml
│
├── background/               # 后台管理端
│   └── admin-frontend/       # 管理后台前端 (Vue 3 + Element Plus)
│       ├── src/
│       │   ├── api/          # API 接口封装
│       │   ├── assets/       # 静态资源 / 样式
│       │   ├── components/   # 公共组件 (侧边栏/顶栏/统计卡片)
│       │   ├── layouts/      # 布局组件
│       │   ├── pages/        # 管理页面
│       │   │   ├── user/     # 用户管理
│       │   │   ├── post/     # 帖子管理
│       │   │   ├── trade/    # 商品管理
│       │   │   ├── comment/  # 评论管理
│       │   │   └── message/  # 消息管理
│       │   ├── router/       # 路由配置
│       │   ├── stores/       # Pinia 状态管理
│       │   └── utils/        # 工具函数
│       ├── index.html
│       ├── package.json
│       ├── vite.config.js
│       └── tailwind.config.js
│
│
├── LICENSE
└── README.md
```

---

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+
- JDK 17+
- MySQL 8.0+
- Redis 7.0+ (可选)

---

### 1. 数据库初始化

```sql
CREATE DATABASE student_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端首次启动时会自动执行数据库迁移：
- 创建所有业务表
- 添加 `role` 字段（兼容旧数据库）
- 创建默认管理员账号 `admin / admin123`

如需手动导入演示数据（含示例帖子和商品），执行 `seed-data.sql`。

---

### 2. 后端启动

编辑 `student-server/src/main/resources/application.yml` 配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

启动后端服务：

```bash
cd student-server
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

---

### 3. 学生端启动

```bash
cd student-app
npm install
npm run dev
```

学生端默认运行在 `http://localhost:3000`。

---

### 4. 管理后台启动

```bash
cd background/admin-frontend
npm install
npm run dev
```

管理后台默认运行在 `http://localhost:3001`。

默认管理员账号：`admin` / `admin123`

---

## API 接口

### 学生端 API（`/api/v1`）

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/v1/auth/**` | 登录/注册/刷新Token |
| 用户 | `/api/v1/user/**` | 用户信息/关注/收藏 |
| 帖子 | `/api/v1/posts/**` | 帖子发布/点赞/收藏 |
| 评论 | `/api/v1/comments/**` | 评论发布/删除 |
| 交易 | `/api/v1/trades/**` | 二手商品发布/收藏 |
| 消息 | `/api/v1/messages/**` | 私信/会话列表 |
| 通知 | `/api/v1/notifications/**` | 系统通知 |
| 上传 | `/api/v1/upload/**` | 图片上传 |

### 管理后台 API（`/api/v1/admin`）

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/v1/admin/auth/login` | 管理员登录 |
| 仪表盘 | `/api/v1/admin/dashboard/**` | 统计数据/趋势图表 |
| 用户管理 | `/api/v1/admin/users/**` | 用户CRUD/启禁用/角色变更/密码重置 |
| 帖子管理 | `/api/v1/admin/posts/**` | 帖子审核/编辑/删除 |
| 商品管理 | `/api/v1/admin/trades/**` | 商品上下架/编辑/删除 |
| 评论管理 | `/api/v1/admin/comments/**` | 评论编辑/删除 |
| 消息管理 | `/api/v1/admin/messages/**` | 消息查看/搜索/删除/会话管理 |

> 管理后台接口需要 `ROLE_ADMIN` 权限，JWT Token 中携带 role 字段。

WebSocket 端点：`/ws`，使用 STOMP 协议。

---

## 功能模块

### 学生端功能

- [x] 用户认证（登录/注册）
- [x] 首页信息流
- [x] 帖子发布与管理
- [x] 二手交易市场
- [x] 商品发布与管理
- [x] 评论系统（支持楼中楼回复）
- [x] 点赞与收藏
- [x] 私信聊天（文字/图片/表情包，WebSocket 实时通信）
- [x] 图片发送（拍照/相册 → 上传 → 图片消息）
- [x] 表情包（80+ 常用 emoji，分类面板）
- [x] 消息通知
- [x] 个人中心
- [x] 搜索功能

### 管理后台功能

- [x] 管理员登录（用户名/手机号）
- [x] 仪表盘（用户/帖子/商品统计、近7日趋势图）
- [x] 用户管理（搜索/筛选/详情/编辑/启禁用/角色变更/密码重置）
- [x] 帖子管理（搜索/筛选/审核/编辑/下架/删除）
- [x] 商品管理（搜索/筛选/上下架/编辑/删除）
- [x] 评论管理（搜索/编辑/删除）
- [x] 消息管理（搜索/筛选/查看详情/删除/会话管理）
- [x] 亮色/暗色主题切换

---

## 设计规范

### 色彩系统

| 色板 | 色值 | 用途 |
|------|------|------|
| 墨色 (Ink) | `#0f1119` → `#f4f4f6` | 文字层级 / 边框 / 背景 |
| 微信绿 (Accent) | `#07C160` | 主色调 / 按钮 / 选中态 |
| 琥珀 (Amber) | `#f59e0b` | 暖色点缀 / 警告 |
| 珊瑚 (Coral) | `#f97316` | 活力色 / 删除 / 强调 |

### 字体

- **标题字体**：DM Serif Display
- **正文字体**：DM Sans
- **等宽字体**：JetBrains Mono

### 设计风格

- **Editorial Ink（编辑油墨）**：深沉、锐利、有温度
- 硬边结构主义：2px 实线边框 + 硬边投影
- 支持亮色/暗色双模式

---

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
