package com.student.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论不能超过500字")
    private String content;

    private Long parentId;
}
