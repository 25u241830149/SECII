package com.campushub.forum.service.impl;

import com.campushub.forum.dto.CommentCreateRequest;
import com.campushub.forum.dto.PostCreateRequest;
import com.campushub.forum.dto.PostDetailDTO;
import com.campushub.forum.dto.PostListDTO;
import com.campushub.forum.service.ForumService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ForumServiceImpl implements ForumService {

    @Override
    public void createPost(PostCreateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public PostDetailDTO getPostDetail(Long postId) {
        return PostDetailDTO.builder()
                .id(postId)
                .viewCount(0)
                .likeCount(0)
                .favoriteCount(0)
                .pinned(Boolean.FALSE)
                .recommended(Boolean.FALSE)
                .build();
    }

    @Override
    public List<PostListDTO> listPosts() {
        return Collections.emptyList();
    }

    @Override
    public void updatePost(Long postId, PostCreateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void deletePost(Long postId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void addComment(Long postId, CommentCreateRequest request) {
        // Scaffold only. Business logic will be implemented later.
    }
}