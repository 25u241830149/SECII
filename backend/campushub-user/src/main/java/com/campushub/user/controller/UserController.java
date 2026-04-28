package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.VerificationSubmitRequest;
import com.campushub.user.entity.User;
import com.campushub.user.service.UserService;
import com.campushub.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;

    @GetMapping("/profile")
    public ApiResponse<UserInfoDTO> getUserInfo(@RequestParam Long userId) {
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<String> updateUserInfo(@RequestParam Long userId, @RequestBody UserProfileDTO profileDTO) {
        User user = User.builder()
                .nickname(profileDTO.getNickname())
                .realName(profileDTO.getRealName())
                .department(profileDTO.getDepartment())
                .avatar(profileDTO.getAvatar())
                .email(profileDTO.getEmail())
                .phone(profileDTO.getPhone())
                .build();
        userService.updateUserInfo(userId, user);
        return ApiResponse.success("User module scaffold ready", null);
    }

    @GetMapping("/credit")
    public ApiResponse<Integer> getUserCredit(@RequestParam Long userId) {
        return ApiResponse.success(userService.getUserCreditScore(userId));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoDTO> getPublicUserInfo(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @GetMapping("/{userId}/home")
    public ApiResponse<UserHomeDTO> getUserHome(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserHome(userId));
    }

    @PostMapping("/verification/submit")
    public ApiResponse<String> submitVerification(@RequestBody VerificationSubmitRequest request) {
        verificationService.submitVerification(request);
        return ApiResponse.success("Verification scaffold ready", null);
    }

    @DeleteMapping("/account")
    public ApiResponse<String> cancelAccount(@RequestParam Long userId) {
        userService.cancelAccount(userId);
        return ApiResponse.success("Account cancellation scaffold ready", null);
    }
}