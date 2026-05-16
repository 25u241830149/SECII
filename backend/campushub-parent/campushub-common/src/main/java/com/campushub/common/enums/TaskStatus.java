package com.campushub.common.enums;

public enum TaskStatus {

    OPEN("OPEN", "待接单"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    OFFLINE("OFFLINE", "已下架");

    private final String value;
    private final String description;

    TaskStatus(String value, String description) {
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
