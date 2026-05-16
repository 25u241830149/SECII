package com.campushub.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortType {

    TIME("time", "按时间排序"),
    HOT("hot", "按热度排序");

    private final String value;
    private final String description;

    SortType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static SortType fromValue(String value) {
        for (SortType sortType : values()) {
            if (sortType.value.equalsIgnoreCase(value)) {
                return sortType;
            }
        }
        throw new IllegalArgumentException("Unsupported sort type: " + value);
    }
}
