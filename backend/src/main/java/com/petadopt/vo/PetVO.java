package com.petadopt.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PetVO {
    private Long id;
    private Long publisherId;
    private String publisherName;
    private String name;
    private String species;
    private String breed;
    private Integer ageMonths;
    private String ageDisplay;
    private Integer gender;
    private String genderDisplay;
    private String healthStatus;
    private List<String> personalityTags;
    private List<String> photos;
    private List<String> videos;
    private String adoptionRequirements;
    private Integer allowSingleLiving;
    private Integer requireExperience;
    private String location;
    private Integer status;
    private String statusDisplay;
    private String remark;
    private LocalDateTime createTime;
}
