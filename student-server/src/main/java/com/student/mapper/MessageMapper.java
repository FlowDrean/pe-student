package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM message WHERE sender_id = #{senderId} AND receiver_id = #{userId} AND is_read = 0")
    int countUnreadFromSender(@Param("userId") Long userId, @Param("senderId") Long senderId);

    @Update("UPDATE message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markConversationRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Update("UPDATE message SET is_read = 1 WHERE id = #{msgId}")
    int markRead(@Param("msgId") Long msgId);
}
