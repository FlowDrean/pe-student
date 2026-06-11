package com.student.enums;

import lombok.Getter;

@Getter
public enum TradeStatus {
    OFFLINE(0, "已下架"),
    SELLING(1, "在售"),
    SOLD(2, "已售"),
    DEALING(3, "交易中");

    private final int code;
    private final String label;

    TradeStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
