package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Comment;
import com.student.entity.dto.CommentDTO;
import com.student.vo.Result;

public interface CommentService extends IService<Comment> {

    /** 获取评论列表 */
    Result<?> getComments(Long postId, int page, int size);

    /** 发表评论 */
    Result<?> createComment(Long userId, Long postId, CommentDTO dto);

    /** 删除评论 */
    void deleteComment(Long userId, Long commentId);

    /** 点赞评论 */
    boolean toggleLike(Long userId, Long commentId);
}
