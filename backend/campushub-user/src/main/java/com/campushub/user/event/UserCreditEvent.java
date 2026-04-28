package com.campushub.user.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreditEvent {

    private Long userId;

    private Integer creditScore;
}