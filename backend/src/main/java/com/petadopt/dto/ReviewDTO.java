package com.petadopt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;
    
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    
    private String comment;

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
