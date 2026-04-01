package com.petadopt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FollowUpDTO {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;
    
    private String content;
    private List<String> photos;
    private List<String> videos;
}
