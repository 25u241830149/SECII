package com.campushub.message.service.impl;

import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.service.NoticeService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Override
    public List<NoticeDTO> listNotices(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public void markNoticeAsRead(Long noticeId) {
        // Scaffold only. Business logic will be implemented later.
    }
}