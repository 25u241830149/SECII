package com.campushub.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailDTO {

    private Long id;

    private Long authorId;

    private String category;

    private String title;

    private String content;

    private String mediaUrls;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Boolean pinned;

    private Boolean recommended;
}