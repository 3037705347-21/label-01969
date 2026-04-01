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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getAgeMonths() { return ageMonths; }
    public void setAgeMonths(Integer ageMonths) { this.ageMonths = ageMonths; }
    public String getAgeDisplay() { return ageDisplay; }
    public void setAgeDisplay(String ageDisplay) { this.ageDisplay = ageDisplay; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getGenderDisplay() { return genderDisplay; }
    public void setGenderDisplay(String genderDisplay) { this.genderDisplay = genderDisplay; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
