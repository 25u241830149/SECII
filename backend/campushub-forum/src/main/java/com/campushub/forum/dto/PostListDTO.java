package com.campushub.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListDTO {

    private Long id;

    private String title;

    private String category;

    private Integer likeCount;

    private Integer favoriteCount;

    private Boolean recommended;
}