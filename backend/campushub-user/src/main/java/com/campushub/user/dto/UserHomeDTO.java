package com.campushub.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHomeDTO {

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer creditScore;

    private String creditLevel;

    private Integer publishedTaskCount;

    private Integer completedOrderCount;
}