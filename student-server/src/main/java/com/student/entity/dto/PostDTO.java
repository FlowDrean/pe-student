package com.student.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PostDTO {
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不能超过2000字")
    private String content;

    private List<String> images;
    private String category;
    private List<String> tags;
}
