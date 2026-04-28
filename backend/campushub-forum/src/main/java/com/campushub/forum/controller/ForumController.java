package com.campushub.forum.controller;

import com.campushub.common.response.ApiResponse;
import com.campushub.common.response.PageResponse;
import com.campushub.forum.dto.CommentCreateRequest;
import com.campushub.forum.dto.PostCreateRequest;
import com.campushub.forum.dto.PostDetailDTO;
import com.campushub.forum.dto.PostListDTO;
import com.campushub.forum.service.ForumService;
import com.campushub.forum.service.PostInteractionService;
import java.util.List;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final PostInteractionService postInteractionService;

    @PostMapping
    public ApiResponse<String> createPost(@RequestBody PostCreateRequest request) {
        forumService.createPost(request);
        return ApiResponse.success("Forum module scaffold ready", null);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailDTO> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.success(forumService.getPostDetail(postId));
    }

    @GetMapping
    public ApiResponse<PageResponse<PostListDTO>> getPostList(@RequestParam(defaultValue = "1") Long page,
                                                              @RequestParam(defaultValue = "10") Long size) {
        List<PostListDTO> posts = forumService.listPosts();
        return ApiResponse.success(new PageResponse<>(posts, (long) posts.size(), page, size));
    }

    @PutMapping("/{postId}")
    public ApiResponse<String> updatePost(@PathVariable Long postId, @RequestBody PostCreateRequest request) {
        forumService.updatePost(postId, request);
        return ApiResponse.success("Forum update scaffold ready", null);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> deletePost(@PathVariable Long postId) {
        forumService.deletePost(postId);
        return ApiResponse.success("Forum delete scaffold ready", null);
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<String> addComment(@PathVariable Long postId, @RequestBody CommentCreateRequest request) {
        forumService.addComment(postId, request);
        return ApiResponse.success("Forum comment scaffold ready", null);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<String> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        postInteractionService.likePost(postId, userId);
        return ApiResponse.success("Forum like scaffold ready", null);
    }

    @PostMapping("/{postId}/favorite")
    public ApiResponse<String> favoritePost(@PathVariable Long postId, @RequestParam Long userId) {
        postInteractionService.favoritePost(postId, userId);
        return ApiResponse.success("Forum favorite scaffold ready", null);
    }
}