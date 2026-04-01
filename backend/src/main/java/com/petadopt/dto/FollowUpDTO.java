package com.petadopt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FollowUpDTO {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;
    
    private String content;
    private List<String> photos;
    private List<String> videos;

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
    public List<String> getVideos() { return videos; }
    public void setVideos(List<String> videos) { this.videos = videos; }
}
