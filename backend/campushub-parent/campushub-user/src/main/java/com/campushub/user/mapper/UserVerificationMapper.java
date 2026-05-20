package com.campushub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.user.entity.UserVerification;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserVerificationMapper extends BaseMapper<UserVerification> {

    UserVerification selectLatestByUserId(@Param("userId") Long userId);

    UserVerification selectPendingByUserId(@Param("userId") Long userId);

    List<UserVerification> selectPendingList();
}
