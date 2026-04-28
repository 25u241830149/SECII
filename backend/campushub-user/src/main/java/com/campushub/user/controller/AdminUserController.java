package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final VerificationService verificationService;

    @PostMapping("/{userId}/ban")
    public ApiResponse<String> banUser(@PathVariable Long userId) {
        return ApiResponse.success("Admin user scaffold ready", null);
    }

    @PostMapping("/{userId}/verify")
    public ApiResponse<String> reviewVerification(@PathVariable Long userId,
                                                  @RequestParam Long reviewerId,
                                                  @RequestParam Boolean approved,
                                                  @RequestParam(required = false) String remark) {
        verificationService.reviewVerification(userId, reviewerId, approved, remark);
        return ApiResponse.success("Admin verification scaffold ready", null);
    }
}