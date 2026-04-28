package com.campushub.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {

    private Long id;

    private Long taskId;

    private Long posterId;

    private Long helperId;

    private String status;
}