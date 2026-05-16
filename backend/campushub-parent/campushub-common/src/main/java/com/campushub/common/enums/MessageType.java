package com.campushub.common.enums;

public enum MessageType {

    ORDER("ORDER", "订单消息"),
    CHAT("CHAT", "聊天消息"),
    NOTICE("NOTICE", "系统公告"),
    SYSTEM("SYSTEM", "系统消息");

    private final String value;
    private final String description;

    MessageType(String value, String description) {
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
