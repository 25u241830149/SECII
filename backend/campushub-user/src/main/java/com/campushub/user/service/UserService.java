package com.campushub.user.service;

import com.campushub.user.dto.UserInfoDTO;
import com.campushub.user.dto.UserHomeDTO;
import com.campushub.user.entity.User;

public interface UserService {

    /**
     * 根据用户 ID 获取用户信息
     */
    UserInfoDTO getUserInfo(Long userId);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User getUserByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    User getUserByPhone(String phone);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, User user);

    /**
     * 获取用户信用分
     */
    Integer getUserCreditScore(Long userId);

    /**
     * 获取个人主页概览
     */
    UserHomeDTO getUserHome(Long userId);

    /**
     * 注销账号
     */
    void cancelAccount(Long userId);

    /**
     * 更新用户信用分
     */
    void updateUserCreditScore(Long userId, Integer score);
}
