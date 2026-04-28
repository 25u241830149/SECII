package com.campushub.forum.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.forum.service.PostInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminForumController {

    private final PostInteractionService postInteractionService;

    @PostMapping("/{postId}/pin")
    public ApiResponse<String> pinPost(@PathVariable Long postId) {
        postInteractionService.pinPost(postId);
        return ApiResponse.success("Admin forum scaffold ready", null);
    }

    @PostMapping("/{postId}/recommend")
    public ApiResponse<String> recommendPost(@PathVariable Long postId) {
        postInteractionService.recommendPost(postId);
        return ApiResponse.success("Admin forum recommend scaffold ready", null);
    }
}