package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.user.dto.CreditDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserProfileUpdateRequest;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.dto.VerificationStatusDTO;
import com.campushub.user.dto.VerificationSubmitRequest;
import com.campushub.user.service.CreditService;
import com.campushub.user.service.UserService;
import com.campushub.user.service.VerificationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final CreditService creditService;
    private final VerificationService verificationService;

    public UserController(
            UserService userService,
            CreditService creditService,
            VerificationService verificationService
    ) {
        this.userService = userService;
        this.creditService = creditService;
        this.verificationService = verificationService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileDTO> getProfile(@RequestParam Long userId) {
        SecurityUtils.requireCurrentUser(userId);
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileDTO> updateProfile(
            @RequestParam Long userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        SecurityUtils.requireCurrentUser(userId);
        return ApiResponse.success(userService.updateProfile(userId, request));
    }

    @GetMapping("/credit")
    public ApiResponse<CreditDTO> getCredit(@RequestParam Long userId) {
        SecurityUtils.requireCurrentUser(userId);
        return ApiResponse.success(creditService.getCredit(userId));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserPublicDTO> getPublicUser(@PathVariable Long userId) {
        return ApiResponse.success(userService.getPublicUser(userId));
    }

    @GetMapping("/{userId}/home")
    public ApiResponse<UserHomeDTO> getHome(@PathVariable Long userId) {
        return ApiResponse.success(userService.getHome(userId));
    }

    @PostMapping("/verification/submit")
    public ApiResponse<VerificationStatusDTO> submitVerification(
            @RequestBody VerificationSubmitRequest request
    ) {
        SecurityUtils.requireCurrentUser(request.userId());
        return ApiResponse.success(verificationService.submit(request));
    }

    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(@RequestParam Long userId) {
        SecurityUtils.requireCurrentUser(userId);
        userService.deleteAccount(userId);
        return ApiResponse.success();
    }
}
