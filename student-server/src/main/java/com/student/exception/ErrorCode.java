package com.student.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "账号已被禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    PHONE_EXISTS(1004, "手机号已注册"),
    PASSWORD_ERROR(1005, "密码错误"),
    CODE_ERROR(1006, "验证码错误"),

    // 帖子相关 2xxx
    POST_NOT_FOUND(2001, "帖子不存在"),
    POST_DELETED(2002, "帖子已删除"),

    // 评论相关 3xxx
    COMMENT_NOT_FOUND(3001, "评论不存在"),

    // 交易相关 4xxx
    TRADE_NOT_FOUND(4001, "商品不存在"),
    TRADE_OFFLINE(4002, "商品已下架"),

    // 消息相关 5xxx
    MESSAGE_SEND_FAILED(5001, "消息发送失败"),

    // 上传相关 6xxx
    UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_TOO_LARGE(6002, "文件大小超出限制"),
    FILE_TYPE_ERROR(6003, "文件类型不支持");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
