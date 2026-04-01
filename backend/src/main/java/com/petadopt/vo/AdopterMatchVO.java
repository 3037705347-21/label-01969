package com.petadopt.vo;

import lombok.Data;

/**
 * 领养人匹配结果VO
 */
@Data
public class AdopterMatchVO {
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 手机号 */
    private String phone;
    
    /** 地址 */
    private String address;
    
    /** 职业 */
    private String occupation;
    
    /** 养宠经验 */
    private String petExperience;
    
    /** 居住环境 */
    private String livingEnvironment;
    
    /** 是否已认证 */
    private Boolean verified;
    
    /** 匹配分数 (0-100) */
    private Integer matchScore;
    
    /** 匹配说明 */
    private String matchReason;
}
