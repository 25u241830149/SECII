package com.campushub.common.response;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, int page, int size) {

    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        return new PageResponse<>(records == null ? List.of() : records, total, page, size);
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), 0L, page, size);
    }
}
