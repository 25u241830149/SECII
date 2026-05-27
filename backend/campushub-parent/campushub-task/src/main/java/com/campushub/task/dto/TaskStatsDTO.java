package com.campushub.task.dto;

public record TaskStatsDTO(
        long todayCreated,
        long inProgress,
        long completed
) {
}
