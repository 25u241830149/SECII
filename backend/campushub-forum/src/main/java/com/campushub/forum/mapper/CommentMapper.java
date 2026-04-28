package com.campushub.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushub.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}