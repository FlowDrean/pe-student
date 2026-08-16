package com.student.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.User;
import com.student.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据初始化器 — 自动添加 role 列并确保管理员账号存在
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        addRoleColumnIfMissing();
        createFollowTableIfMissing();
        addReplyToUserIdColumnIfMissing();
        fixNullRoles();
        initAdminUser();
    }

    /**
     * 兼容已有数据库：如果 comment 表没有 reply_to_user_id 列则自动添加（楼中楼回复）
     */
    private void addReplyToUserIdColumnIfMissing() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT reply_to_user_id FROM comment LIMIT 1");
            rs.close();
            log.info("reply_to_user_id 列已存在，跳过 DDL");
        } catch (Exception e) {
            log.info("reply_to_user_id 列不存在，正在自动添加...");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "ALTER TABLE `comment` ADD COLUMN `reply_to_user_id` BIGINT DEFAULT NULL " +
                    "COMMENT '被回复的用户ID（楼中楼回复时使用）' AFTER `parent_id`");
                log.info("reply_to_user_id 列添加成功");
            } catch (Exception ex) {
                log.error("添加 reply_to_user_id 列失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 兼容已有数据库：如果 follow 表不存在则自动创建（关注/粉丝功能）
     */
    private void createFollowTableIfMissing() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id FROM follow LIMIT 1");
            rs.close();
            log.info("follow 表已存在，跳过 DDL");
        } catch (Exception e) {
            log.info("follow 表不存在，正在自动创建...");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS `follow` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL COMMENT '关注者ID'," +
                    "`following_id` BIGINT NOT NULL COMMENT '被关注者ID'," +
                    "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY `uk_user_following` (`user_id`, `following_id`)," +
                    "INDEX `idx_user_id` (`user_id`)," +
                    "INDEX `idx_following_id` (`following_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表'");
                log.info("follow 表创建成功");
            } catch (Exception ex) {
                log.error("创建 follow 表失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 兼容已有数据库：如果 user 表没有 role 列则自动添加
     */
    private void addRoleColumnIfMissing() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT role FROM user LIMIT 1");
            rs.close();
            log.info("role 列已存在，跳过 DDL");
        } catch (Exception e) {
            log.warn("role 列不存在，正在自动添加...");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'user' " +
                    "COMMENT '角色: user-普通用户 admin-管理员'");
                log.info("role 列添加成功");
            } catch (Exception ex) {
                log.error("添加 role 列失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 修复旧数据的 NULL role：ALTER TABLE 的 DEFAULT 只对新行生效，
     * 已有行仍是 NULL，需要统一设为 'user'
     */
    private void fixNullRoles() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(
                "UPDATE `user` SET `role` = 'user' WHERE `role` IS NULL");
            if (updated > 0) {
                log.info("已修复 {} 条用户的 role 字段（NULL → user）", updated);
            }
        } catch (Exception e) {
            log.warn("修复 NULL role 失败（可能列尚未创建）: {}", e.getMessage());
        }
    }

    private void initAdminUser() {
        try {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getRole, "admin"));
            if (count > 0) {
                log.info("管理员账号已存在，跳过初始化");
                return;
            }

            // 检查 admin 用户名是否被占用（从旧数据迁移过来的普通用户）
            User existing = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
            if (existing != null) {
                existing.setRole("admin");
                userMapper.updateById(existing);
                log.info("已将用户 admin 升级为管理员");
            } else {
                User admin = new User();
                admin.setUsername("admin");
                admin.setNickname("系统管理员");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("admin");
                admin.setStatus(1);
                userMapper.insert(admin);
                log.info("管理员账号初始化完成: admin / admin123");
            }
        } catch (Exception e) {
            log.error("初始化管理员账号失败: {}", e.getMessage());
        }
    }
}
