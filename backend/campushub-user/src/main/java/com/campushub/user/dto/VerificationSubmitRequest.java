package com.campushub.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSubmitRequest {

    private Long userId;

    private String realName;

    private String studentId;

    private String college;

    private String documentUrl;
}