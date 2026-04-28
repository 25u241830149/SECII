package com.campushub.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListDTO {

    private Long id;

    private String type;

    private String content;

    private Boolean read;
}