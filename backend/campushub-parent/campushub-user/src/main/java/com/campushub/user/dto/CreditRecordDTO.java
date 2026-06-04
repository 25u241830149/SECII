package com.campushub.user.dto;

import java.time.OffsetDateTime;

public record CreditRecordDTO(
        Long recordId,
        String reason,
        Integer delta,
        Integer scoreAfter,
        OffsetDateTime createdAt
) {
}
