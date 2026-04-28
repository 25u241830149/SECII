package com.campushub.forum.service;

import com.campushub.forum.dto.CommentCreateRequest;
import com.campushub.forum.dto.PostCreateRequest;
import com.campushub.forum.dto.PostDetailDTO;
import com.campushub.forum.dto.PostListDTO;
import java.util.List;

public interface ForumService {

    void createPost(PostCreateRequest request);

    PostDetailDTO getPostDetail(Long postId);

    List<PostListDTO> listPosts();

    void updatePost(Long postId, PostCreateRequest request);

    void deletePost(Long postId);

    void addComment(Long postId, CommentCreateRequest request);
}