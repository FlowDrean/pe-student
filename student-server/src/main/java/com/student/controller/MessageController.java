package com.student.controller;

import com.student.entity.dto.MessageDTO;
import com.student.service.MessageService;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtil securityUtil;

    @GetMapping("/conversations")
    public Result<?> getConversations() {
        return Result.ok(messageService.getConversations(securityUtil.getCurrentUserId()));
    }

    @GetMapping("/{userId}")
    public Result<?> getMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messageService.getMessages(securityUtil.getCurrentUserId(), userId, page, size);
    }

    @PostMapping("/{userId}")
    public Result<?> sendMessage(@PathVariable Long userId, @Valid @RequestBody MessageDTO dto) {
        return Result.ok(messageService.sendMessage(securityUtil.getCurrentUserId(), userId, dto));
    }

    @DeleteMapping("/{userId}")
    public Result<Void> deleteConversation(@PathVariable Long userId) {
        messageService.deleteConversation(securityUtil.getCurrentUserId(), userId);
        return Result.ok();
    }

    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        return Result.ok(messageService.getUnreadCount(securityUtil.getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.ok();
    }
}
