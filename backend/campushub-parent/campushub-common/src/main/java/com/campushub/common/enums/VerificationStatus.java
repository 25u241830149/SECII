package com.campushub.common.enums;

public enum VerificationStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String value;
    private final String description;

    VerificationStatus(String value, String description) {
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
