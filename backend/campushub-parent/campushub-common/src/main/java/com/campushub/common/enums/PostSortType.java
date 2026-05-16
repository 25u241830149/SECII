package com.campushub.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostSortType {

    LATEST("latest", "按最新排序"),
    VIEWS("views", "按浏览量排序"),
    RECOMMEND("recommend", "按推荐排序");

    private final String value;
    private final String description;

    PostSortType(String value, String description) {
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
    public static PostSortType fromValue(String value) {
        for (PostSortType sortType : values()) {
            if (sortType.value.equalsIgnoreCase(value)) {
                return sortType;
            }
        }
        throw new IllegalArgumentException("Unsupported post sort type: " + value);
    }
}
