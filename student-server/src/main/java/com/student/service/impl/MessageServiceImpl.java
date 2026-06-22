package com.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Message;
import com.student.entity.User;
import com.student.entity.dto.MessageDTO;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.MessageMapper;
import com.student.mapper.UserMapper;
import com.student.service.MessageService;
import com.student.vo.MessageVO;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<MessageVO> getConversations(Long userId) {
        // 获取最近的私信
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
                .or()
                .eq(Message::getSenderId, userId);
        wrapper.orderByDesc(Message::getCreatedAt);

        List<Message> messages = list(wrapper);

        // 按会话分组
        Map<Long, MessageVO> convMap = new LinkedHashMap<>();
        for (Message msg : messages) {
            Long peerId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (!convMap.containsKey(peerId)) {
                MessageVO vo = new MessageVO();
                vo.setUserId(peerId);
                // 图片消息在会话列表中显示 [图片]
                vo.setLastMessage(msg.getType() != null && msg.getType() == 2 ? "[图片]" : msg.getContent());
                vo.setLastTime(msg.getCreatedAt());

                User user = userMapper.selectById(peerId);
                if (user != null) {
                    vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    vo.setAvatar(user.getAvatar());
                }
                convMap.put(peerId, vo);
            }
        }

        // 补充未读数（每个会话单独统计）
        convMap.forEach((peerId, vo) -> {
            vo.setUnread(baseMapper.countUnreadFromSender(userId, peerId));
        });

        return new ArrayList<>(convMap.values());
    }

    @Override
    public Result<?> getMessages(Long userId, Long peerId, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId)
                .or()
                .eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId));
        wrapper.orderByDesc(Message::getCreatedAt);

        var pageResult = page(new Page<>(page, size), wrapper);
        List<Message> list = pageResult.getRecords();
        Collections.reverse(list); // 按时间正序

        // 标记已读
        baseMapper.markConversationRead(peerId, userId);

        // 推送已读状态
        messagingTemplate.convertAndSendToUser(
                String.valueOf(peerId), "/queue/read",
                Map.of("senderId", userId));

        return Result.ok(PageVO.from(pageResult));
    }

    @Override
    @Transactional
    public MessageVO sendMessage(Long senderId, Long receiverId, MessageDTO dto) {
        Integer type = dto.getType() != null ? dto.getType() : 1;

        // 图片消息校验
        if (type == 2) {
            if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
                throw new BusinessException("图片消息必须上传图片");
            }
        } else {
            // 文字消息校验
            if (dto.getContent() == null || dto.getContent().isBlank()) {
                throw new BusinessException("消息内容不能为空");
            }
        }

        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(type == 2 ? "[图片]" : dto.getContent());
        msg.setType(type);
        msg.setImageUrl(dto.getImageUrl());
        msg.setIsRead(0);
        save(msg);

        // WebSocket 实时推送
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId), "/queue/message", msg);

        return BeanUtil.copyProperties(msg, MessageVO.class);
    }

    @Override
    public void deleteConversation(Long userId, Long peerId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId)
                .or()
                .eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId));
        remove(wrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long msgId) {
        baseMapper.markRead(msgId);
    }
}
