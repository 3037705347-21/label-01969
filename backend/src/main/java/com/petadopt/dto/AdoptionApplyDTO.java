package com.petadopt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdoptionApplyDTO {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    @NotBlank(message = "自我介绍不能为空")
    @Size(min = 20, max = 1000, message = "自我介绍需要20-1000个字符")
    private String selfIntroduction;
    
    @Pattern(regexp = "^$|^(https?://|/uploads/).+", message = "居住证明链接格式不正确")
    private String residenceProof;
    
    @Pattern(regexp = "^$|^(https?://|/uploads/).+", message = "收入证明链接格式不正确")
    private String incomeProof;
}
