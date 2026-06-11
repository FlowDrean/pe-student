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

        List<Map<String, Object>> list = pageResult.getRecords().stream().map(c -> {
            Map<String, Object> map = BeanUtil.beanToMap(c);
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                map.put("user", BeanUtil.copyProperties(user, UserVO.class));
            }
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
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        comment.setLikes(0);
        comment.setStatus(1);
        save(comment);

        // 更新帖子评论数
        postMapper.incrComments(postId, 1);

        // 发送通知
        if (!post.getUserId().equals(userId)) {
            User commenter = userMapper.selectById(userId);
            String commenterName = commenter != null ? (commenter.getNickname() != null ? commenter.getNickname() : commenter.getUsername()) : "有人";
            String preview = dto.getContent();
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            notificationService.createNotification(post.getUserId(), 2,
                    "新的评论", commenterName + " 评论了你：「" + preview + "」", postId, 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        User user = userMapper.selectById(userId);
        if (user != null) {
            result.put("user", BeanUtil.copyProperties(user, UserVO.class));
        }
        return Result.ok(result);
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
