package com.campushub.message.mapper;

import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.entity.Notice;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoticeMapper {

    int insertNotice(Notice notice);

    int updateNotice(Notice notice);

    int deleteNotice(@Param("noticeId") Long noticeId);

    Notice selectNoticeById(@Param("noticeId") Long noticeId);

    NoticeDTO selectNoticeDetail(@Param("noticeId") Long noticeId);

    List<NoticeDTO> selectNotices(
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countNotices();

    List<NoticeDTO> selectLatestNotices(@Param("limit") int limit);
}
