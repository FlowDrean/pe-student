package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关注者ID */
    private Long userId;
    /** 被关注者ID */
    private Long followingId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
