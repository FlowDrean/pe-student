package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Comment;
import com.student.entity.LikeRecord;
import com.student.entity.Post;
import com.student.entity.User;
import com.student.entity.dto.CommentDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.CommentMapper;
import com.student.mapper.LikeRecordMapper;
import com.student.mapper.PostMapper;
import com.student.mapper.UserMapper;
import com.student.service.CommentService;
import com.student.service.NotificationService;
import com.student.vo.PageVO;
import com.student.vo.Result;
import com.student.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final NotificationService notificationService;

    @Override
    public Result<?> getComments(Long postId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .eq(Comment::getParentId, 0)
                .orderByDesc(Comment::getCreatedAt);

        var pageResult = page(new Page<>(page, size), wrapper);

        // 一次性查出本页所有一级评论的回复（楼中楼）
        List<Long> parentIds = pageResult.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        Map<Long, List<Map<String, Object>>> repliesMap = new HashMap<>();
        Map<Long, User> userCache = new HashMap<>();
        if (!parentIds.isEmpty()) {
            List<Comment> replies = list(new LambdaQueryWrapper<Comment>()
                    .in(Comment::getParentId, parentIds)
                    .eq(Comment::getStatus, 1)
                    .orderByAsc(Comment::getCreatedAt));
            for (Comment r : replies) {
                Map<String, Object> map = BeanUtil.beanToMap(r);
                User user = userCache.computeIfAbsent(r.getUserId(), id -> userMapper.selectById(id));
                if (user != null) {
                    map.put("user", BeanUtil.copyProperties(user, UserVO.class));
                }
                if (r.getReplyToUserId() != null) {
                    User replyTo = userCache.computeIfAbsent(r.getReplyToUserId(), id -> userMapper.selectById(id));
                    if (replyTo != null) {
                        map.put("replyToName", replyTo.getNickname() != null ? replyTo.getNickname() : replyTo.getUsername());
                    }
                }
                repliesMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(map);
            }
        }

        List<Map<String, Object>> list = pageResult.getRecords().stream().map(c -> {
            Map<String, Object> map = BeanUtil.beanToMap(c);
            User user = userCache.computeIfAbsent(c.getUserId(), id -> userMapper.selectById(id));
            if (user != null) {
                map.put("user", BeanUtil.copyProperties(user, UserVO.class));
            }
            map.put("replies", repliesMap.getOrDefault(c.getId(), List.of()));
            return map;
        }).collect(Collectors.toList());

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(pageResult.getTotal());
        vo.setPage(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return Result.ok(vo);
    }

    @Override
    @Transactional
    public Result<?> createComment(Long userId, Long postId, CommentDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setLikes(0);
        comment.setStatus(1);

        // 楼中楼：父评论本身是回复时，归到一级评论下，并记录被回复人
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Comment parent = getById(dto.getParentId());
            if (parent != null && parent.getParentId() != null && parent.getParentId() > 0) {
                comment.setParentId(parent.getParentId());
                comment.setReplyToUserId(dto.getReplyToUserId() != null ? dto.getReplyToUserId() : parent.getUserId());
            } else {
                comment.setParentId(dto.getParentId());
                comment.setReplyToUserId(dto.getReplyToUserId());
            }
        } else {
            comment.setParentId(0L);
        }
        save(comment);

        // 更新帖子评论数
        postMapper.incrComments(postId, 1);

        User commenter = userMapper.selectById(userId);
        String commenterName = commenter != null ? (commenter.getNickname() != null ? commenter.getNickname() : commenter.getUsername()) : "有人";

        // 通知帖子作者（自己评论自己的帖子除外）
        if (!post.getUserId().equals(userId)) {
            String preview = previewOf(dto.getContent());
            notificationService.createNotification(post.getUserId(), 2,
                    "新的评论", commenterName + " 评论了你：「" + preview + "」", postId, 1);
        }

        // 通知被回复的评论作者（楼中楼）
        if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(userId)
                && !comment.getReplyToUserId().equals(post.getUserId())) {
            String preview = previewOf(dto.getContent());
            notificationService.createNotification(comment.getReplyToUserId(), 2,
                    "新的回复", commenterName + " 回复了你：「" + preview + "」", postId, 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        User user = userMapper.selectById(userId);
        if (user != null) {
            result.put("user", BeanUtil.copyProperties(user, UserVO.class));
        }
        if (comment.getReplyToUserId() != null) {
            User replyTo = userMapper.selectById(comment.getReplyToUserId());
            if (replyTo != null) {
                result.put("replyToName", replyTo.getNickname() != null ? replyTo.getNickname() : replyTo.getUsername());
            }
        }
        return Result.ok(result);
    }

    private String previewOf(String content) {
        if (content != null && content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        return content;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        if (!comment.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        comment.setStatus(0);
        updateById(comment);
        postMapper.incrComments(comment.getPostId(), -1);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long commentId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, commentId)
                .eq(LikeRecord::getTargetType, 2);

        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null) {
            likeRecordMapper.deleteById(record.getId());
            baseMapper.incrLikes(commentId, -1);
            return false;
        } else {
            LikeRecord newRecord = new LikeRecord();
            newRecord.setUserId(userId);
            newRecord.setTargetId(commentId);
            newRecord.setTargetType(2);
            likeRecordMapper.insert(newRecord);
            baseMapper.incrLikes(commentId, 1);
            return true;
        }
    }
}
