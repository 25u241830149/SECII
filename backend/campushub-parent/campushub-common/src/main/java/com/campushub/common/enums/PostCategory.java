package com.campushub.common.enums;

public enum PostCategory {

    HELP("HELP", "互助求助"),
    STUDY("STUDY", "学习交流"),
    TRADE("TRADE", "闲置交易"),
    LOST_FOUND("LOST_FOUND", "失物招领"),
    TEAM_UP("TEAM_UP", "组队匹配"),
    OTHER("OTHER", "其他");

    private final String value;
    private final String description;

    PostCategory(String value, String description) {
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
