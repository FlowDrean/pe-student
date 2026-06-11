package com.student.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String school;
    private String major;
    private String bio;
    private Integer gender;
    private Integer status;
    private Integer followCount;
    private Integer fansCount;
    private Integer postCount;
    private Integer likedCount;
    private Boolean isFollowed;
    private LocalDateTime createdAt;
}
