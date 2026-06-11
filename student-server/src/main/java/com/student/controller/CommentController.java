package com.student.controller;

import com.student.entity.dto.CommentDTO;
import com.student.service.CommentService;
import com.student.utils.SecurityUtil;
import com.student.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/api/v1/posts/{postId}/comments")
    public Result<?> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return commentService.getComments(postId, page, size);
    }

    @PostMapping("/api/v1/posts/{postId}/comments")
    public Result<?> createComment(@PathVariable Long postId, @Valid @RequestBody CommentDTO dto) {
        return commentService.createComment(securityUtil.getCurrentUserId(), postId, dto);
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(securityUtil.getCurrentUserId(), id);
        return Result.ok();
    }

    @PostMapping("/api/v1/comments/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        return Result.ok(commentService.toggleLike(securityUtil.getCurrentUserId(), id));
    }
}
