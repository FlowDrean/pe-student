package com.student.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private List<String> images;
    private String category;
    private List<String> tags;
    private Integer likes;
    private Integer comments;
    private Integer views;
    private Boolean isLiked;
    private Boolean isFavorited;
    private UserVO user;
    private LocalDateTime createdAt;
}
