package com.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.User;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.UserMapper;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(result.getRecords().stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("school", u.getSchool());
            map.put("gender", u.getGender());
            map.put("status", u.getStatus());
            map.put("role", u.getRole());
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).toList());
        vo.setTotal(result.getTotal());
        vo.setPage(result.getCurrent());
        vo.setSize(result.getSize());
        vo.setPages(result.getPages());
        return Result.ok(vo);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("school", user.getSchool());
        map.put("major", user.getMajor());
        map.put("studentId", user.getStudentId());
        map.put("bio", user.getBio());
        map.put("gender", user.getGender());
        map.put("status", user.getStatus());
        map.put("role", user.getRole());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        return Result.ok(map);
    }

    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "不能禁用管理员账号");
        }
        Integer status = body.get("status");
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("school")) user.setSchool((String) body.get("school"));
        if (body.containsKey("major")) user.setMajor((String) body.get("major"));
        if (body.containsKey("studentId")) user.setStudentId((String) body.get("studentId"));
        if (body.containsKey("bio")) user.setBio((String) body.get("bio"));
        if (body.containsKey("gender")) user.setGender((Integer) body.get("gender"));
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(body.get("password")));
        userMapper.updateById(user);
        return Result.ok();
    }

    @PutMapping("/{id}/role")
    public Result<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String role = body.get("role");
        user.setRole(role);
        userMapper.updateById(user);
        return Result.ok();
    }
}
