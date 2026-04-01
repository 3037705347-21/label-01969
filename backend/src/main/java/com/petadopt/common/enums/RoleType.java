package com.petadopt.common.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN(1, "管理员"),
    RESCUER(2, "救助方"),
    ADOPTER(3, "领养人");

    private final Integer code;
    private final String name;

    RoleType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (RoleType type : values()) {
            if (type.getCode().equals(code)) {
                return type.getName();
            }
        }
        return "未知";
    }
}
