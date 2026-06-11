package com.student.controller;

import com.student.entity.dto.PostDTO;
import com.student.service.PostService;
import com.student.utils.SecurityUtil;
import com.student.vo.PostVO;
import com.student.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public Result<?> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = null;
        try { userId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.ok(postService.getPostList(userId, category, page, size));
    }

    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        Long userId = null;
        try { userId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.ok(postService.getPostDetail(userId, id));
    }

    @PostMapping
    public Result<PostVO> createPost(@Valid @RequestBody PostDTO dto) {
        return Result.ok(postService.createPost(securityUtil.getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<Void> updatePost(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        postService.updatePost(securityUtil.getCurrentUserId(), id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(securityUtil.getCurrentUserId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        return Result.ok(postService.toggleLike(securityUtil.getCurrentUserId(), id));
    }

    @PostMapping("/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        return Result.ok(postService.toggleFavorite(securityUtil.getCurrentUserId(), id));
    }

    @GetMapping("/categories")
    public Result<?> getCategories() {
        return Result.ok(java.util.List.of(
                java.util.Map.of("key", "help", "label", "求助", "icon", "🤝"),
                java.util.Map.of("key", "food", "label", "美食", "icon", "🍜"),
                java.util.Map.of("key", "study", "label", "学习", "icon", "📚"),
                java.util.Map.of("key", "life", "label", "生活", "icon", "🏠"),
                java.util.Map.of("key", "other", "label", "其他", "icon", "💬")
        ));
    }

    @GetMapping("/hot")
    public Result<?> getHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.getHotPosts(page, size));
    }

    @GetMapping("/search")
    public Result<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.searchPosts(keyword, page, size));
    }
}
