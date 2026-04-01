package com.petadopt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyDTO {
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度2-20个字符")
    private String realName;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
             message = "身份证号格式不正确")
    private String idCard;
    
    @Size(max = 200, message = "地址不能超过200个字符")
    private String address;
    
    @Size(max = 50, message = "职业不能超过50个字符")
    private String occupation;
    
    @Size(max = 500, message = "养宠经验描述不能超过500个字符")
    private String petExperience;
    
    @Size(max = 500, message = "居住环境描述不能超过500个字符")
    private String livingEnvironment;
    
    @Size(max = 100, message = "机构名称不能超过100个字符")
    private String orgName;
    
    @Pattern(regexp = "^$|^(https?://|/uploads/).+", message = "机构资质链接格式不正确")
    private String orgLicense;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
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
}
