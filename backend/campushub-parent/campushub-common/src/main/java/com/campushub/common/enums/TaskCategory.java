package com.campushub.common.enums;

public enum TaskCategory {

    EXPRESS("EXPRESS", "快递代取"),
    STUDY("STUDY", "学习辅导"),
    SECOND_HAND("SECOND_HAND", "二手交易"),
    TEAM_UP("TEAM_UP", "组队匹配"),
    OTHER("OTHER", "其他");

    private final String value;
    private final String description;

    TaskCategory(String value, String description) {
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
