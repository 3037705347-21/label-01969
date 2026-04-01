package com.petadopt.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class PetDTO {
    private Long id;
    @NotBlank(message = "宠物名称不能为空")
    @Size(max = 50, message = "宠物名称不能超过50个字符")
    private String name;
    @NotBlank(message = "物种不能为空")
    @Size(max = 20, message = "物种不能超过20个字符")
    private String species;
    @Size(max = 50, message = "品种不能超过50个字符")
    private String breed;
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄最小为1个月")
    @Max(value = 360, message = "年龄最大为360个月")
    private Integer ageMonths;
    @NotNull(message = "性别不能为空")
    private Integer gender;
    @Size(max = 200, message = "健康状况描述不能超过200个字符")
    private String healthStatus;
    @Size(max = 10, message = "性格标签最多10个")
    private List<String> personalityTags;
    @Size(max = 9, message = "照片最多9张")
    private List<String> photos;
    @Size(max = 3, message = "视频最多3个")
    private List<String> videos;
    @Size(max = 500, message = "领养要求描述不能超过500个字符")
    private String adoptionRequirements;
    private Integer allowSingleLiving;
    private Integer requireExperience;
    @NotBlank(message = "所在地区不能为空")
    @Size(max = 100, message = "所在地区不能超过100个字符")
    private String location;
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getAgeMonths() { return ageMonths; }
    public void setAgeMonths(Integer ageMonths) { this.ageMonths = ageMonths; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public List<String> getPersonalityTags() { return personalityTags; }
    public void setPersonalityTags(List<String> personalityTags) { this.personalityTags = personalityTags; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public List<String> getVideos() { return videos; }
    public void setVideos(List<String> videos) { this.videos = videos; }
    public String getAdoptionRequirements() { return adoptionRequirements; }
    public void setAdoptionRequirements(String adoptionRequirements) { this.adoptionRequirements = adoptionRequirements; }
    public Integer getAllowSingleLiving() { return allowSingleLiving; }
    public void setAllowSingleLiving(Integer allowSingleLiving) { this.allowSingleLiving = allowSingleLiving; }
    public Integer getRequireExperience() { return requireExperience; }
    public void setRequireExperience(Integer requireExperience) { this.requireExperience = requireExperience; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
