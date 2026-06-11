package com.student.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    LIKE(1, "点赞"),
    COMMENT(2, "评论"),
    FOLLOW(3, "关注"),
    SYSTEM(4, "系统"),
    TRADE(5, "交易");

    private final int code;
    private final String label;

    NotificationType(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
