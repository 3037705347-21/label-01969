package com.petadopt.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer roleType;
    private String roleName;
    private String address;
    private String occupation;
    private String petExperience;
    private String livingEnvironment;
    private String orgName;
    private String orgLicense;
    private Integer status;
    private Integer verifyStatus;
    private LocalDateTime createTime;
}
