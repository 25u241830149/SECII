package com.campushub.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String nickname;

    private String realName;

    private String department;

    private String avatar;

    private String role;

    private Integer creditScore;
}
