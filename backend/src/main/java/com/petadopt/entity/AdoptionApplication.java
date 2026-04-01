package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("adoption_application")
public class AdoptionApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long petId;
    private Long applicantId;
    
    /** 自我介绍 */
    private String selfIntroduction;
    
    /** 居住证明(文件路径) */
    private String residenceProof;
    
    /** 收入证明(文件路径) */
    private String incomeProof;
    
    /** 救助方审核状态: 0待审核 1通过 2拒绝 */
    private Integer rescueReviewStatus;
    private String rescueReviewComment;
    private LocalDateTime rescueReviewTime;
    
    /** 管理员复核状态: 0待审核 1通过 2拒绝 */
    private Integer adminReviewStatus;
    private String adminReviewComment;
    private LocalDateTime adminReviewTime;
    
    /** 家访状态: 0未安排 1已安排 2已完成 3不通过 */
    private Integer homeVisitStatus;
    private String homeVisitComment;
    private LocalDateTime homeVisitTime;
    
    /** 最终状态: 0进行中 1成功 2失败 */
    private Integer finalStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
