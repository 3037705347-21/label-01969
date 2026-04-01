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
}
