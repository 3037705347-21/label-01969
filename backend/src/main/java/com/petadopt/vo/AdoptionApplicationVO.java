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
}
