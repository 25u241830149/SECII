package com.campushub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.user.dto.AdminUserOptionDTO;
import com.campushub.user.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    User selectActiveById(@Param("id") Long id);

    User selectActiveByStudentId(@Param("studentId") String studentId);

    int existsByStudentId(@Param("studentId") String studentId);

    List<AdminUserOptionDTO> selectAdminUserOptions(
            @Param("keyword") String keyword,
            @Param("role") Integer role,
            @Param("limit") int limit
    );

    @Update("""
            UPDATE u_user
            SET credit_score = LEAST(100, GREATEST(0, credit_score + #{delta})),
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{userId} AND is_deleted = false
            """)
    int adjustCreditScore(@Param("userId") Long userId, @Param("delta") int delta);

    @Select("""
            SELECT credit_score
            FROM u_user
            WHERE id = #{userId} AND is_deleted = false
            """)
    Integer selectCreditScore(@Param("userId") Long userId);
}
