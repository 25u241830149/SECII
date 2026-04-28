package com.campushub.forum.service;

public interface PostInteractionService {

    void likePost(Long postId, Long userId);

    void favoritePost(Long postId, Long userId);

    void pinPost(Long postId);

    void recommendPost(Long postId);
}