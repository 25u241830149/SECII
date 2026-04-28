package com.campushub.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    private Long authorId;

    private String category;

    private String title;

    private String content;

    private String mediaUrls;
}