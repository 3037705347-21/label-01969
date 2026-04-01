package com.petadopt.common.enums;

import lombok.Getter;

@Getter
public enum PetStatus {
    AVAILABLE(1, "待领养"),
    REVIEWING(2, "审核中"),
    ADOPTED(3, "已领养"),
    UNAVAILABLE(4, "暂不领养");

    private final Integer code;
    private final String name;

    PetStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (PetStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return "未知";
    }
}
