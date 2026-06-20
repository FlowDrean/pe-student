package com.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Comment;
import com.student.entity.Post;
import com.student.entity.User;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.CommentMapper;
import com.student.mapper.PostMapper;
import com.student.mapper.UserMapper;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long postId) {

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Comment::getContent, keyword);
        }
        if (postId != null) {
            wrapper.eq(Comment::getPostId, postId);
        }
        wrapper.orderByDesc(Comment::getCreatedAt);

        IPage<Comment> result = commentMapper.selectPage(new Page<>(page, size), wrapper);

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(result.getRecords().stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("postId", c.getPostId());
            map.put("parentId", c.getParentId());
            map.put("likes", c.getLikes());
            map.put("status", c.getStatus());
            map.put("createdAt", c.getCreatedAt());

            User commenter = userMapper.selectById(c.getUserId());
            if (commenter != null) {
                Map<String, Object> commenterMap = new LinkedHashMap<>();
                commenterMap.put("id", commenter.getId());
                commenterMap.put("username", commenter.getUsername());
                commenterMap.put("nickname", commenter.getNickname());
                commenterMap.put("avatar", commenter.getAvatar());
                map.put("user", commenterMap);
            }

            Post post = postMapper.selectById(c.getPostId());
            if (post != null) {
                map.put("postTitle", post.getTitle());
            }
            return map;
        }).toList());
        vo.setTotal(result.getTotal());
        vo.setPage(result.getCurrent());
        vo.setSize(result.getSize());
        vo.setPages(result.getPages());
        return Result.ok(vo);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (body.containsKey("content")) comment.setContent((String) body.get("content"));
        if (body.containsKey("status")) comment.setStatus((Integer) body.get("status"));
        commentMapper.updateById(comment);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
        return Result.ok();
    }
}
