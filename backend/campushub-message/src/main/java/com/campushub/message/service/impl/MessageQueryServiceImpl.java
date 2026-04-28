package com.campushub.message.service.impl;

import com.campushub.common.response.PageResponse;
import com.campushub.message.dto.MessageListDTO;
import com.campushub.message.service.MessageQueryService;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class MessageQueryServiceImpl implements MessageQueryService {

    @Override
    public PageResponse<MessageListDTO> getMessages(Long userId, Long page, Long size) {
        return new PageResponse<>(Collections.emptyList(), 0L, page, size);
    }
}