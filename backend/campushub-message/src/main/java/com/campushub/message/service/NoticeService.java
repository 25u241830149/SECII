package com.campushub.message.service;

import com.campushub.message.dto.NoticeDTO;
import java.util.List;

public interface NoticeService {

    List<NoticeDTO> listNotices(Long userId);

    void markNoticeAsRead(Long noticeId);
}