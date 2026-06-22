package com.student.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageDTO {
    @Size(max = 500, message = "消息不能超过500字")
    private String content;

    private Integer type;

    private String imageUrl;
}
