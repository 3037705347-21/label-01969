package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String module;
    private String action;
    private String params;
    private String result;
    private String ip;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
