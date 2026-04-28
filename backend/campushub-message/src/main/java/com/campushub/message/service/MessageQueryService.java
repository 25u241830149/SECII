package com.campushub.message.service;

import com.campushub.common.response.PageResponse;
import com.campushub.message.dto.MessageListDTO;

public interface MessageQueryService {

    PageResponse<MessageListDTO> getMessages(Long userId, Long page, Long size);
}