package com.campushub.message.mapper;

import com.campushub.message.dto.MessageDTO;
import com.campushub.message.entity.Message;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageMapper {

    int insertMessage(Message message);

    int insertMessagesForActiveUsers(
            @Param("type") Integer type,
            @Param("title") String title,
            @Param("content") String content,
            @Param("excludeUserId") Long excludeUserId
    );

    List<MessageDTO> selectMessages(
            @Param("receiverId") Long receiverId,
            @Param("type") Integer type,
            @Param("unread") Boolean unread,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countMessages(
            @Param("receiverId") Long receiverId,
            @Param("type") Integer type,
            @Param("unread") Boolean unread
    );

    long countUnread(@Param("receiverId") Long receiverId);

    int markRead(@Param("messageId") Long messageId, @Param("receiverId") Long receiverId);

    int markAllRead(@Param("receiverId") Long receiverId);

    int deleteMessage(@Param("messageId") Long messageId, @Param("receiverId") Long receiverId);
}
