package com.campushub.message.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {

    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private Boolean read;

    private LocalDateTime createdAt;
}