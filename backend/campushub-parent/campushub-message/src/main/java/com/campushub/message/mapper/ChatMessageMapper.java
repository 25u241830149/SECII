package com.campushub.message.mapper;

import com.campushub.message.dto.ChatMessageDTO;
import com.campushub.message.entity.ChatMessage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChatMessageMapper {

    int insertChatMessage(ChatMessage message);

    List<ChatMessageDTO> selectChatMessages(@Param("orderId") Long orderId);

    int markOrderMessagesRead(@Param("orderId") Long orderId, @Param("receiverId") Long receiverId);
}
