package com.campushub.common.enums;

public enum OrderStatus {

    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String value;
    private final String description;

    OrderStatus(String value, String description) {
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
