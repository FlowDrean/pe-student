package com.student.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "用户名/手机号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
