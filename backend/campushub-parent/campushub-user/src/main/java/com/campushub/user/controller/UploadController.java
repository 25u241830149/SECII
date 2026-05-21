package com.campushub.user.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.user.dto.UploadResultDTO;
import com.campushub.user.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadResultDTO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(uploadService.uploadAvatar(userId, file));
    }

    @PostMapping("/student-card")
    public ApiResponse<UploadResultDTO> uploadStudentCard(
            @RequestParam("file") MultipartFile file,
            @RequestParam String studentId
    ) {
        return ApiResponse.success(uploadService.uploadStudentCard(studentId, file));
    }
}
