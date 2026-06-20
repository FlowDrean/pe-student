package com.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.User;
import com.student.entity.dto.LoginDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.UserMapper;
import com.student.utils.JwtUtil;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityUtil securityUtil;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO dto) {
        log.info("管理员登录尝试: username={}", dto.getUsername());

        // 支持用户名或手机号登录
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
                        .or()
                        .eq(User::getPhone, dto.getUsername()));

        if (user == null) {
            log.warn("管理员登录失败: 用户不存在, username={}", dto.getUsername());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!"admin".equals(user.getRole())) {
            log.warn("管理员登录失败: 非管理员账号, username={}, role={}", dto.getUsername(), user.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "该账号不是管理员，无法登录后台");
        }
        if (user.getStatus() == 0) {
            log.warn("管理员登录失败: 账号已禁用, username={}", dto.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("管理员登录失败: 密码错误, username={}", dto.getUsername());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        log.info("管理员登录成功: username={}, userId={}", dto.getUsername(), user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", String.valueOf(user.getId()));
        return Result.ok(result);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("role", user.getRole());
        return Result.ok(info);
    }
}
