package com.campushub.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_verification")
public class UserVerification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String studentId;

    private String college;

    private String documentUrl;

    private String status;

    private Long reviewerId;

    private String reviewRemark;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}