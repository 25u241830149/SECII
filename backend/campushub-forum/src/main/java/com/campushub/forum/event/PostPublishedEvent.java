package com.campushub.forum.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostPublishedEvent {

    private Long postId;

    private Long authorId;
}