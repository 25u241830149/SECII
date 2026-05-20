package com.campushub.user.service;

import com.campushub.common.enums.UserRole;
import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.dto.UserProfileDTO;
import com.campushub.user.dto.UserPublicDTO;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;

final class UserDtoAssembler {

    private UserDtoAssembler() {
    }

    static UserInfoDTO toUserInfo(User user, UserVerification verification) {
        return new UserInfoDTO(
                user.getId(),
                user.getStudentId(),
                user.getNickname(),
                user.getAvatarUrl(),
                roleName(user.getRole()),
                user.getCreditScore(),
                verificationStatusName(verification)
        );
    }

    static UserProfileDTO toUserProfile(User user, UserVerification verification) {
        return new UserProfileDTO(
                user.getId(),
                user.getStudentId(),
                user.getNickname(),
                user.getAvatarUrl(),
                roleName(user.getRole()),
                user.getCreditScore(),
                verificationStatusName(verification),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    static UserPublicDTO toUserPublic(User user) {
        return new UserPublicDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl(),
                null,
                user.getCreditScore(),
                CreditService.creditLevel(user.getCreditScore() == null ? 0 : user.getCreditScore())
        );
    }

    static UserRole roleFromCode(Integer role) {
        return Integer.valueOf(1).equals(role) ? UserRole.ADMIN : UserRole.USER;
    }

    static String roleName(Integer role) {
        return roleFromCode(role).name();
    }

    static String verificationStatusName(UserVerification verification) {
        if (verification == null || verification.getStatus() == null) {
            return "PENDING";
        }
        return switch (verification.getStatus()) {
            case 1 -> "APPROVED";
            case 2 -> "REJECTED";
            default -> "PENDING";
        };
    }
}
