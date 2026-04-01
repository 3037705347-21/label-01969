package com.petadopt.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FollowUpRecordVO {
    private Long id;
    private Long petId;
    private String petName;
    private Long adopterId;
    private String adopterName;
    private Long applicationId;
    private String content;
    private List<String> photos;
    private List<String> videos;
    private Integer status;
    private String statusDisplay;
    private String adminComment;
    private LocalDateTime dueDate;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
}
