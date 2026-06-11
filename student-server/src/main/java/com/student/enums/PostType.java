package com.student.enums;

import lombok.Getter;

@Getter
public enum PostType {
    HELP("help", "求助"),
    FOOD("food", "美食"),
    STUDY("study", "学习"),
    LIFE("life", "生活"),
    OTHER("other", "其他");

    private final String code;
    private final String label;

    PostType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
