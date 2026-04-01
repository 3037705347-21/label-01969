package com.petadopt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 发布者ID */
    private Long publisherId;
    
    private String name;
    
    /** 物种: 猫/狗/其他 */
    private String species;
    
    /** 品种 */
    private String breed;
    
    /** 年龄(月) */
    private Integer ageMonths;
    
    /** 性别: 1公 2母 */
    private Integer gender;
    
    /** 健康状况 */
    private String healthStatus;
    
    /** 性格标签(JSON数组) */
    private String personalityTags;
    
    /** 照片(JSON数组) */
    private String photos;
    
    /** 视频(JSON数组) */
    private String videos;
    
    /** 领养要求描述 */
    private String adoptionRequirements;
    
    /** 是否允许独居: 0否 1是 */
    private Integer allowSingleLiving;
    
    /** 是否要求有养宠经验: 0否 1是 */
    private Integer requireExperience;
    
    /** 所在地区 */
    private String location;
    
    /** 领养人ID */
    private Long adopterId;
    
    /** 状态: 1待领养 2审核中 3已领养 4暂不领养 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
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
    public String getPersonalityTags() { return personalityTags; }
    public void setPersonalityTags(String personalityTags) { this.personalityTags = personalityTags; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getVideos() { return videos; }
    public void setVideos(String videos) { this.videos = videos; }
    public String getAdoptionRequirements() { return adoptionRequirements; }
    public void setAdoptionRequirements(String adoptionRequirements) { this.adoptionRequirements = adoptionRequirements; }
    public Integer getAllowSingleLiving() { return allowSingleLiving; }
    public void setAllowSingleLiving(Integer allowSingleLiving) { this.allowSingleLiving = allowSingleLiving; }
    public Integer getRequireExperience() { return requireExperience; }
    public void setRequireExperience(Integer requireExperience) { this.requireExperience = requireExperience; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getAdopterId() { return adopterId; }
    public void setAdopterId(Long adopterId) { this.adopterId = adopterId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
