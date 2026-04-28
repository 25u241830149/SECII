package com.campushub.bootstrap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {

    private Long userCount;

    private Long taskCount;

    private Long orderCount;

    private Long forumPostCount;

    private Long pendingVerificationCount;
}