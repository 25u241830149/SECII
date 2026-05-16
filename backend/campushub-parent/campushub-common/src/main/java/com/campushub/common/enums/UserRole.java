package com.campushub.common.enums;

public enum UserRole {

    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    private final String value;
    private final String description;

    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
