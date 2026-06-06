package com.campushub.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.message.dto.NoticeCreateRequest;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.entity.Notice;
import com.campushub.message.mapper.NoticeMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeMapper noticeMapper;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    void latestCapsLimitAtTen() {
        NoticeDTO dto = noticeDetail(21L, "Title", "Body");
        when(noticeMapper.selectLatestNotices(10)).thenReturn(List.of(dto));

        List<NoticeDTO> notices = noticeService.latest(99);

        assertEquals(List.of(dto), notices);
    }

    @Test
    void createTrimsPayloadAndReturnsDetail() {
        when(noticeMapper.insertNotice(any(Notice.class))).thenAnswer(invocation -> {
            Notice notice = invocation.getArgument(0);
            notice.setId(21L);
            return 1;
        });
        NoticeDTO detail = noticeDetail(21L, "Launch", "CampusHub live");
        when(noticeMapper.selectNoticeDetail(21L)).thenReturn(detail);

        NoticeDTO result = noticeService.create(1L, new NoticeCreateRequest("  Launch  ", "  CampusHub live  "));

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeMapper).insertNotice(captor.capture());
        assertEquals("Launch", captor.getValue().getTitle());
        assertEquals("CampusHub live", captor.getValue().getContent());
        assertEquals(detail, result);
    }

    @Test
    void updateRejectsMissingNotice() {
        when(noticeMapper.selectNoticeById(21L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeService.update(21L, new NoticeCreateRequest("Title", "Body"))
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void deleteRejectsMissingNotice() {
        when(noticeMapper.deleteNotice(21L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeService.delete(21L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    private static NoticeDTO noticeDetail(Long noticeId, String title, String content) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new NoticeDTO(noticeId, 1L, "Admin", title, content, now, now);
    }
}
