package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge")
public class Knowledge {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String content;
    
    /** 分类: 1养宠常识 2领养须知 3疾病护理 */
    private Integer category;
    
    private Long authorId;
    
    /** 状态: 0草稿 1发布 */
    private Integer status;
    
    private Integer viewCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
