package com.student.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer type;
    private Integer isRead;
    private LocalDateTime createdAt;

    // 会话列表用
    private String nickname;
    private String avatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unread;
    private Long userId;
}
