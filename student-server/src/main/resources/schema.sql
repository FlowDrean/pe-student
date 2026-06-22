-- ============================================
-- 校园互助平台 — 数据库建表脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS student_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE student_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `school` VARCHAR(100) COMMENT '学校',
    `major` VARCHAR(100) COMMENT '专业',
    `student_id` VARCHAR(50) COMMENT '学号',
    `bio` VARCHAR(200) COMMENT '个人简介',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user-普通用户 admin-管理员',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 帖子表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(200) COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `images` JSON COMMENT '图片URL数组',
    `category` VARCHAR(50) COMMENT '分类',
    `tags` JSON COMMENT '标签数组',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `comments` INT DEFAULT 0 COMMENT '评论数',
    `views` INT DEFAULT 0 COMMENT '浏览数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常 2-审核中',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 点赞记录表
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-评论',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    INDEX `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 二手交易表
CREATE TABLE IF NOT EXISTS `trade` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `images` JSON COMMENT '商品图片',
    `category` VARCHAR(50) COMMENT '分类',
    `condition` TINYINT COMMENT '成色: 1-全新 2-九成新 3-八成新 4-七成新及以下',
    `location` VARCHAR(200) COMMENT '交易地点',
    `contact` VARCHAR(100) COMMENT '联系方式',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-在售 2-已售 3-交易中',
    `views` INT DEFAULT 0 COMMENT '浏览数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`),
    INDEX `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手交易表';

-- 私信表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-文字 2-图片 3-系统消息',
    `image_url` VARCHAR(500) COMMENT '图片URL（type=2时使用）',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_conversation` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `type` TINYINT NOT NULL COMMENT '类型: 1-点赞 2-评论 3-关注 4-系统 5-交易',
    `title` VARCHAR(200) COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `target_id` BIGINT COMMENT '关联目标ID',
    `target_type` TINYINT COMMENT '关联目标类型',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-商品',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 兼容已有数据库：如果 user 表已存在但没有 role 列，执行此语句
-- ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user-普通用户 admin-管理员';
-- 管理员账号通过 Spring Boot DataInitializer 在应用启动时自动创建
