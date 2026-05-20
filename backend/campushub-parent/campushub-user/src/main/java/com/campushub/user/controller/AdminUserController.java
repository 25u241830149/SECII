package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.user.dto.AdminBanRequest;
import com.campushub.user.dto.AdminBanResultDTO;
import com.campushub.user.dto.AdminVerifyRequest;
import com.campushub.user.dto.AdminVerifyResultDTO;
import com.campushub.user.service.UserService;
import com.campushub.user.service.VerificationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final VerificationService verificationService;

    public AdminUserController(UserService userService, VerificationService verificationService) {
        this.userService = userService;
        this.verificationService = verificationService;
    }

    @PostMapping("/{userId}/ban")
    public ApiResponse<AdminBanResultDTO> banUser(
            @PathVariable Long userId,
            @RequestBody AdminBanRequest request
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(userService.banUser(userId, request));
    }

    @PostMapping("/{userId}/verify")
    public ApiResponse<AdminVerifyResultDTO> verifyUser(
            @PathVariable Long userId,
            @RequestBody AdminVerifyRequest request
    ) {
        SecurityUtils.requireAdmin();
        Long reviewerId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(verificationService.review(userId, reviewerId, request));
    }
}
