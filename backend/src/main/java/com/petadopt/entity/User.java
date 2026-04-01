package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    
    /** 角色类型: 1管理员 2救助方 3领养人 */
    private Integer roleType;
    
    private String idCard;
    private String address;
    private String occupation;
    
    /** 养宠经验 */
    private String petExperience;
    
    /** 居住环境描述 */
    private String livingEnvironment;
    
    /** 机构名称(救助方) */
    private String orgName;
    
    /** 机构资质证明(救助方) */
    private String orgLicense;
    
    /** 状态: 0禁用 1正常 2黑名单 */
    private Integer status;
    
    /** 认证状态: 0未认证 1已认证 */
    private Integer verifyStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
