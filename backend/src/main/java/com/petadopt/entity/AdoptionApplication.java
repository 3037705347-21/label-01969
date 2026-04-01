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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getSelfIntroduction() { return selfIntroduction; }
    public void setSelfIntroduction(String selfIntroduction) { this.selfIntroduction = selfIntroduction; }
    public String getResidenceProof() { return residenceProof; }
    public void setResidenceProof(String residenceProof) { this.residenceProof = residenceProof; }
    public String getIncomeProof() { return incomeProof; }
    public void setIncomeProof(String incomeProof) { this.incomeProof = incomeProof; }
    public Integer getRescueReviewStatus() { return rescueReviewStatus; }
    public void setRescueReviewStatus(Integer rescueReviewStatus) { this.rescueReviewStatus = rescueReviewStatus; }
    public String getRescueReviewComment() { return rescueReviewComment; }
    public void setRescueReviewComment(String rescueReviewComment) { this.rescueReviewComment = rescueReviewComment; }
    public LocalDateTime getRescueReviewTime() { return rescueReviewTime; }
    public void setRescueReviewTime(LocalDateTime rescueReviewTime) { this.rescueReviewTime = rescueReviewTime; }
    public Integer getAdminReviewStatus() { return adminReviewStatus; }
    public void setAdminReviewStatus(Integer adminReviewStatus) { this.adminReviewStatus = adminReviewStatus; }
    public String getAdminReviewComment() { return adminReviewComment; }
    public void setAdminReviewComment(String adminReviewComment) { this.adminReviewComment = adminReviewComment; }
    public LocalDateTime getAdminReviewTime() { return adminReviewTime; }
    public void setAdminReviewTime(LocalDateTime adminReviewTime) { this.adminReviewTime = adminReviewTime; }
    public Integer getHomeVisitStatus() { return homeVisitStatus; }
    public void setHomeVisitStatus(Integer homeVisitStatus) { this.homeVisitStatus = homeVisitStatus; }
    public String getHomeVisitComment() { return homeVisitComment; }
    public void setHomeVisitComment(String homeVisitComment) { this.homeVisitComment = homeVisitComment; }
    public LocalDateTime getHomeVisitTime() { return homeVisitTime; }
    public void setHomeVisitTime(LocalDateTime homeVisitTime) { this.homeVisitTime = homeVisitTime; }
    public Integer getFinalStatus() { return finalStatus; }
    public void setFinalStatus(Integer finalStatus) { this.finalStatus = finalStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
