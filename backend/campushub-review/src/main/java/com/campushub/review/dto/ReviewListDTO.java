package com.campushub.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListDTO {

    private Long id;

    private Long orderId;

    private Integer score;

    private String comment;
}