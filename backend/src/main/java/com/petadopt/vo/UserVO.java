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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getRoleType() { return roleType; }
    public void setRoleType(Integer roleType) { this.roleType = roleType; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getPetExperience() { return petExperience; }
    public void setPetExperience(String petExperience) { this.petExperience = petExperience; }
    public String getLivingEnvironment() { return livingEnvironment; }
    public void setLivingEnvironment(String livingEnvironment) { this.livingEnvironment = livingEnvironment; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgLicense() { return orgLicense; }
    public void setOrgLicense(String orgLicense) { this.orgLicense = orgLicense; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(Integer verifyStatus) { this.verifyStatus = verifyStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
