package com.student.controller;

import com.student.entity.dto.UserDTO;
import com.student.service.UserService;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import com.student.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        return Result.ok(userService.getProfile(securityUtil.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserDTO dto) {
        userService.updateProfile(securityUtil.getCurrentUserId(), dto);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        Long currentUserId = null;
        try { currentUserId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.ok(userService.getUserById(currentUserId, id));
    }

    @GetMapping("/{id}/posts")
    public Result<?> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = null;
        try { currentUserId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.ok(userService.getUserPosts(currentUserId, id, page, size));
    }

    @GetMapping("/{id}/trades")
    public Result<?> getUserTrades(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = null;
        try { currentUserId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.ok(userService.getUserTrades(currentUserId, id, page, size));
    }

    @GetMapping("/favorites")
    public Result<?> getUserFavorites(
            @RequestParam(defaultValue = "1") int targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.getUserFavorites(securityUtil.getCurrentUserId(), targetType, page, size));
    }

    @PostMapping("/follow/{id}")
    public Result<Boolean> follow(@PathVariable Long id) {
        return Result.ok(userService.toggleFollow(securityUtil.getCurrentUserId(), id));
    }

    @DeleteMapping("/follow/{id}")
    public Result<Boolean> unfollow(@PathVariable Long id) {
        return Result.ok(userService.toggleFollow(securityUtil.getCurrentUserId(), id));
    }
}
