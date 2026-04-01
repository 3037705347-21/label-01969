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
}
