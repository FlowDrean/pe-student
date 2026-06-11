package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Message;
import com.student.entity.dto.MessageDTO;
import com.student.vo.MessageVO;
import com.student.vo.Result;

import java.util.List;

public interface MessageService extends IService<Message> {

    /** 获取会话列表 */
    List<MessageVO> getConversations(Long userId);

    /** 获取聊天记录 */
    Result<?> getMessages(Long userId, Long peerId, int page, int size);

    /** 发送消息 */
    MessageVO sendMessage(Long senderId, Long receiverId, MessageDTO dto);

    /** 删除会话 */
    void deleteConversation(Long userId, Long peerId);

    /** 获取未读消息数 */
    int getUnreadCount(Long userId);

    /** 标记消息已读 */
    void markRead(Long msgId);
}
