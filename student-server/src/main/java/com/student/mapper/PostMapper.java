package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE post SET likes = likes + #{delta} WHERE id = #{postId}")
    int incrLikes(@Param("postId") Long postId, @Param("delta") int delta);

    @Update("UPDATE post SET comments = comments + #{delta} WHERE id = #{postId}")
    int incrComments(@Param("postId") Long postId, @Param("delta") int delta);

    @Update("UPDATE post SET views = views + 1 WHERE id = #{postId}")
    int incrViews(@Param("postId") Long postId);

    @Select("SELECT COALESCE(SUM(likes), 0) FROM post WHERE user_id = #{userId} AND status = 1")
    int sumLikesByUserId(@Param("userId") Long userId);
}
