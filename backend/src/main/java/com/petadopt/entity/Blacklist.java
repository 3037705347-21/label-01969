package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /** 类型: 1领养人 2救助方 */
    private Integer type;
    
    /** 拉黑原因 */
    private String reason;
    
    /** 操作人ID */
    private Long operatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
