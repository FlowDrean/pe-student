package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE comment SET likes = likes + #{delta} WHERE id = #{commentId}")
    int incrLikes(@Param("commentId") Long commentId, @Param("delta") int delta);

    /** 统计单个帖子的评论数（仅 status=1） */
    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId} AND status = 1 AND parent_id = 0")
    int countByPostId(@Param("postId") Long postId);

    /** 批量统计帖子评论数 */
    @Select("<script>" +
            "SELECT post_id AS postId, COUNT(*) AS cnt " +
            "FROM comment " +
            "WHERE post_id IN <foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND status = 1 AND parent_id = 0 " +
            "GROUP BY post_id" +
            "</script>")
    List<Map<String, Object>> countByPostIds(@Param("postIds") List<Long> postIds);
}
