package com.petadopt.vo;

import lombok.Data;

@Data
public class AdopterMatchVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String address;
    private String occupation;
    private String petExperience;
    private String livingEnvironment;
    private Boolean verified;
    private Integer matchScore;
    private String matchReason;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getPetExperience() { return petExperience; }
    public void setPetExperience(String petExperience) { this.petExperience = petExperience; }
    public String getLivingEnvironment() { return livingEnvironment; }
    public void setLivingEnvironment(String livingEnvironment) { this.livingEnvironment = livingEnvironment; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
}
