package com.petadopt.common.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING(0, "进行中"),
    SUCCESS(1, "领养成功"),
    FAILED(2, "领养失败");

    private final Integer code;
    private final String name;

    ApplicationStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (ApplicationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return "未知";
    }
}
