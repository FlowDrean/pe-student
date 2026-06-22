package com.student.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Message;
import com.student.entity.User;
import com.student.exception.BusinessException;
import com.student.exception.ErrorCode;
import com.student.mapper.MessageMapper;
import com.student.mapper.UserMapper;
import com.student.vo.PageVO;
import com.student.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @GetMapping
    public Result<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Message::getContent, keyword);
        }
        if (type != null) {
            wrapper.eq(Message::getType, type);
        }
        wrapper.orderByDesc(Message::getCreatedAt);

        IPage<Message> result = messageMapper.selectPage(new Page<>(page, size), wrapper);

        PageVO<Map<String, Object>> vo = new PageVO<>();
        vo.setList(result.getRecords().stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSenderId());
            map.put("receiverId", m.getReceiverId());
            map.put("content", m.getContent());
            map.put("type", m.getType());
            map.put("imageUrl", m.getImageUrl());
            map.put("isRead", m.getIsRead());
            map.put("createdAt", m.getCreatedAt());

            // 发送者信息
            User sender = userMapper.selectById(m.getSenderId());
            if (sender != null) {
                Map<String, Object> senderMap = new LinkedHashMap<>();
                senderMap.put("id", sender.getId());
                senderMap.put("username", sender.getUsername());
                senderMap.put("nickname", sender.getNickname());
                senderMap.put("avatar", sender.getAvatar());
                map.put("sender", senderMap);
            }

            // 接收者信息
            User receiver = userMapper.selectById(m.getReceiverId());
            if (receiver != null) {
                Map<String, Object> receiverMap = new LinkedHashMap<>();
                receiverMap.put("id", receiver.getId());
                receiverMap.put("username", receiver.getUsername());
                receiverMap.put("nickname", receiver.getNickname());
                receiverMap.put("avatar", receiver.getAvatar());
                map.put("receiver", receiverMap);
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
        Message msg = messageMapper.selectById(id);
        if (msg == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", msg.getId());
        map.put("senderId", msg.getSenderId());
        map.put("receiverId", msg.getReceiverId());
        map.put("content", msg.getContent());
        map.put("type", msg.getType());
        map.put("imageUrl", msg.getImageUrl());
        map.put("isRead", msg.getIsRead());
        map.put("createdAt", msg.getCreatedAt());

        User sender = userMapper.selectById(msg.getSenderId());
        if (sender != null) {
            Map<String, Object> senderMap = new LinkedHashMap<>();
            senderMap.put("id", sender.getId());
            senderMap.put("username", sender.getUsername());
            senderMap.put("nickname", sender.getNickname());
            map.put("sender", senderMap);
        }
        User receiver = userMapper.selectById(msg.getReceiverId());
        if (receiver != null) {
            Map<String, Object> receiverMap = new LinkedHashMap<>();
            receiverMap.put("id", receiver.getId());
            receiverMap.put("username", receiver.getUsername());
            receiverMap.put("nickname", receiver.getNickname());
            map.put("receiver", receiverMap);
        }
        return Result.ok(map);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Message msg = messageMapper.selectById(id);
        if (msg == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        messageMapper.deleteById(id);
        return Result.ok();
    }

    @DeleteMapping("/conversation")
    public Result<?> deleteConversation(@RequestParam Long userId, @RequestParam Long peerId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId)
                .or()
                .eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId));
        messageMapper.delete(wrapper);
        return Result.ok();
    }
}
