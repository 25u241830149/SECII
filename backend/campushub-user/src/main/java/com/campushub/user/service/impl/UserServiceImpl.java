package com.campushub.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.entity.User;
import com.campushub.user.mapper.UserMapper;
import com.campushub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public void updateUserInfo(Long userId, User user) {
        user.setId(userId);
        userMapper.updateById(user);
    }

    @Override
    public Integer getUserCreditScore(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getCreditScore() : 0;
    }

    @Override
    public UserHomeDTO getUserHome(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return UserHomeDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .creditScore(user.getCreditScore())
                .creditLevel(resolveCreditLevel(user.getCreditScore()))
                .publishedTaskCount(0)
                .completedOrderCount(0)
                .build();
    }

    @Override
    public void cancelAccount(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus("CANCELLED");
        userMapper.updateById(user);
    }

    @Override
    public void updateUserCreditScore(Long userId, Integer score) {
        User user = new User();
        user.setId(userId);
        user.setCreditScore(score);
        userMapper.updateById(user);
    }

    private UserInfoDTO convertToDTO(User user) {
        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .department(user.getDepartment())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .creditScore(user.getCreditScore())
                .build();
    }

    private String resolveCreditLevel(Integer creditScore) {
        if (creditScore == null) {
            return "BRONZE";
        }
        if (creditScore >= 90) {
            return "GOLD";
        }
        if (creditScore >= 75) {
            return "SILVER";
        }
        return "BRONZE";
    }
}
