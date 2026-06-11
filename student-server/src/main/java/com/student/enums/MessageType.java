package com.student.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT(1, "文字"),
    IMAGE(2, "图片"),
    SYSTEM(3, "系统消息");

    private final int code;
    private final String label;

    MessageType(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
