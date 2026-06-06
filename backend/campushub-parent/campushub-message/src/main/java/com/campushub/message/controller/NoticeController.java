package com.campushub.message.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.message.dto.NoticeCreateRequest;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.service.NoticeService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/api/notices/latest")
    public ApiResponse<List<NoticeDTO>> latest(@RequestParam(required = false) Integer limit) {
        return ApiResponse.success(noticeService.latest(limit));
    }

    @GetMapping("/api/admin/notices")
    public ApiResponse<PageResponse<NoticeDTO>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(noticeService.list(page, size));
    }

    @PostMapping("/api/admin/notices")
    public ApiResponse<NoticeDTO> create(@RequestBody NoticeCreateRequest request) {
        SecurityUtils.requireAdmin();
        Long currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ApiResponse.success(noticeService.create(currentUserId, request));
    }

    @PutMapping("/api/admin/notices/{noticeId}")
    public ApiResponse<NoticeDTO> update(@PathVariable Long noticeId, @RequestBody NoticeCreateRequest request) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(noticeService.update(noticeId, request));
    }

    @DeleteMapping("/api/admin/notices/{noticeId}")
    public ApiResponse<Void> delete(@PathVariable Long noticeId) {
        SecurityUtils.requireAdmin();
        noticeService.delete(noticeId);
        return ApiResponse.success();
    }
}
