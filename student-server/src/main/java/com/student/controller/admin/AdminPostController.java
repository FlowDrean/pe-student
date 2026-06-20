package com.student.controller.admin;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Post;
import com.student.entity.User;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.PostMapper;
import com.student.mapper.UserMapper;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Post::getTitle, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Post::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        IPage<Post> result = postMapper.selectPage(new Page<>(page, size), wrapper);

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(result.getRecords().stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("title", p.getTitle());
            map.put("content", p.getContent());
            map.put("category", p.getCategory());
            map.put("tags", p.getTags() != null ? JSONUtil.toList(p.getTags(), String.class) : null);
            map.put("images", p.getImages() != null ? JSONUtil.toList(p.getImages(), String.class) : null);
            map.put("likes", p.getLikes());
            map.put("comments", p.getComments());
            map.put("views", p.getViews());
            map.put("isTop", p.getIsTop());
            map.put("status", p.getStatus());
            map.put("userId", p.getUserId());
            map.put("createdAt", p.getCreatedAt());
            map.put("updatedAt", p.getUpdatedAt());
            // 作者信息
            User author = userMapper.selectById(p.getUserId());
            if (author != null) {
                Map<String, Object> authorMap = new LinkedHashMap<>();
                authorMap.put("id", author.getId());
                authorMap.put("username", author.getUsername());
                authorMap.put("nickname", author.getNickname());
                authorMap.put("avatar", author.getAvatar());
                map.put("author", authorMap);
            }
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
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("category", post.getCategory());
        map.put("tags", post.getTags() != null ? JSONUtil.toList(post.getTags(), String.class) : null);
        map.put("images", post.getImages() != null ? JSONUtil.toList(post.getImages(), String.class) : null);
        map.put("likes", post.getLikes());
        map.put("comments", post.getComments());
        map.put("views", post.getViews());
        map.put("isTop", post.getIsTop());
        map.put("status", post.getStatus());
        map.put("createdAt", post.getCreatedAt());

        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            Map<String, Object> authorMap = new LinkedHashMap<>();
            authorMap.put("id", author.getId());
            authorMap.put("username", author.getUsername());
            authorMap.put("nickname", author.getNickname());
            authorMap.put("avatar", author.getAvatar());
            map.put("author", authorMap);
        }
        return Result.ok(map);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        post.setStatus(body.get("status"));
        postMapper.updateById(post);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if (body.containsKey("title")) post.setTitle((String) body.get("title"));
        if (body.containsKey("content")) post.setContent((String) body.get("content"));
        if (body.containsKey("category")) post.setCategory((String) body.get("category"));
        if (body.containsKey("tags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) body.get("tags");
            post.setTags(tags != null ? JSONUtil.toJsonStr(tags) : null);
        }
        if (body.containsKey("isTop")) post.setIsTop((Integer) body.get("isTop"));
        if (body.containsKey("status")) post.setStatus((Integer) body.get("status"));
        postMapper.updateById(post);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        post.setStatus(0); // 逻辑删除
        postMapper.updateById(post);
        return Result.ok();
    }
}
