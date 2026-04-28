package com.campushub.forum.service.impl;

import com.campushub.forum.service.PostInteractionService;
import org.springframework.stereotype.Service;

@Service
public class PostInteractionServiceImpl implements PostInteractionService {

    @Override
    public void likePost(Long postId, Long userId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void favoritePost(Long postId, Long userId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void pinPost(Long postId) {
        // Scaffold only. Business logic will be implemented later.
    }

    @Override
    public void recommendPost(Long postId) {
        // Scaffold only. Business logic will be implemented later.
    }
}