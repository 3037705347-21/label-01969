package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_record")
public class FollowUpRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long petId;
    private Long adopterId;
    private Long applicationId;
    
    /** 跟进内容 */
    private String content;
    
    /** 照片(JSON数组) */
    private String photos;
    
    /** 视频(JSON数组) */
    private String videos;
    
    /** 状态: 1正常 2异常 */
    private Integer status;
    
    /** 管理员评论 */
    private String adminComment;
    
    /** 应提交日期 */
    private LocalDateTime dueDate;
    
    /** 实际提交时间 */
    private LocalDateTime submitTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
