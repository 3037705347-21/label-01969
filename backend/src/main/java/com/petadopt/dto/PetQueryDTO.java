package com.petadopt.dto;

import lombok.Data;

@Data
public class PetQueryDTO {
    private String species;
    private String breed;
    private Integer minAge;
    private Integer maxAge;
    private Integer gender;
    private String location;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
