package com.petadopt.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdoptionApplicationVO {
    private Long id;
    private Long petId;
    private String petName;
    private String petPhoto;
    private Long applicantId;
    private String applicantName;
    private String applicantPhone;
    private String selfIntroduction;
    private String residenceProof;
    private String incomeProof;
    private Integer rescueReviewStatus;
    private String rescueReviewComment;
    private LocalDateTime rescueReviewTime;
    private Integer adminReviewStatus;
    private String adminReviewComment;
    private LocalDateTime adminReviewTime;
    private Integer homeVisitStatus;
    private String homeVisitComment;
    private LocalDateTime homeVisitTime;
    private Integer finalStatus;
    private String finalStatusDisplay;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getPetPhoto() { return petPhoto; }
    public void setPetPhoto(String petPhoto) { this.petPhoto = petPhoto; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    public String getSelfIntroduction() { return selfIntroduction; }
    public void setSelfIntroduction(String selfIntroduction) { this.selfIntroduction = selfIntroduction; }
    public String getResidenceProof() { return residenceProof; }
    public void setResidenceProof(String residenceProof) { this.residenceProof = residenceProof; }
    public String getIncomeProof() { return incomeProof; }
    public void setIncomeProof(String incomeProof) { this.incomeProof = incomeProof; }
    public Integer getRescueReviewStatus() { return rescueReviewStatus; }
    public void setRescueReviewStatus(Integer rescueReviewStatus) { this.rescueReviewStatus = rescueReviewStatus; }
    public String getRescueReviewComment() { return rescueReviewComment; }
    public void setRescueReviewComment(String rescueReviewComment) { this.rescueReviewComment = rescueReviewComment; }
    public LocalDateTime getRescueReviewTime() { return rescueReviewTime; }
    public void setRescueReviewTime(LocalDateTime rescueReviewTime) { this.rescueReviewTime = rescueReviewTime; }
    public Integer getAdminReviewStatus() { return adminReviewStatus; }
    public void setAdminReviewStatus(Integer adminReviewStatus) { this.adminReviewStatus = adminReviewStatus; }
    public String getAdminReviewComment() { return adminReviewComment; }
    public void setAdminReviewComment(String adminReviewComment) { this.adminReviewComment = adminReviewComment; }
    public LocalDateTime getAdminReviewTime() { return adminReviewTime; }
    public void setAdminReviewTime(LocalDateTime adminReviewTime) { this.adminReviewTime = adminReviewTime; }
    public Integer getHomeVisitStatus() { return homeVisitStatus; }
    public void setHomeVisitStatus(Integer homeVisitStatus) { this.homeVisitStatus = homeVisitStatus; }
    public String getHomeVisitComment() { return homeVisitComment; }
    public void setHomeVisitComment(String homeVisitComment) { this.homeVisitComment = homeVisitComment; }
    public LocalDateTime getHomeVisitTime() { return homeVisitTime; }
    public void setHomeVisitTime(LocalDateTime homeVisitTime) { this.homeVisitTime = homeVisitTime; }
    public Integer getFinalStatus() { return finalStatus; }
    public void setFinalStatus(Integer finalStatus) { this.finalStatus = finalStatus; }
    public String getFinalStatusDisplay() { return finalStatusDisplay; }
    public void setFinalStatusDisplay(String finalStatusDisplay) { this.finalStatusDisplay = finalStatusDisplay; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
