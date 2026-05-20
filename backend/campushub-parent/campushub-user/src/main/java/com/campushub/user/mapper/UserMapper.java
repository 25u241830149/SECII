package com.campushub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.user.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    User selectActiveById(@Param("id") Long id);

    User selectActiveByStudentId(@Param("studentId") String studentId);

    int existsByStudentId(@Param("studentId") String studentId);
}
