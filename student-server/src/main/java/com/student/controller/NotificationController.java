package com.student.controller;

import com.student.service.NotificationService;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public Result<?> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return notificationService.getNotifications(securityUtil.getCurrentUserId(), page, size);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(securityUtil.getCurrentUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        return Result.ok(notificationService.getUnreadCount(securityUtil.getCurrentUserId()));
    }
}
