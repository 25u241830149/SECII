package com.campushub.message.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.message.dto.NoticeCreateRequest;
import com.campushub.message.dto.NoticeDTO;
import com.campushub.message.entity.Notice;
import com.campushub.message.mapper.NoticeMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

    private static final int MAX_TITLE_LENGTH = 128;

    private final NoticeMapper noticeMapper;
    private final MessageService messageService;

    public NoticeService(NoticeMapper noticeMapper, MessageService messageService) {
        this.noticeMapper = noticeMapper;
        this.messageService = messageService;
    }

    public List<NoticeDTO> latest(Integer limit) {
        int normalizedLimit = limit == null ? 5 : Math.max(1, Math.min(limit, 10));
        return noticeMapper.selectLatestNotices(normalizedLimit);
    }

    public PageResponse<NoticeDTO> list(Integer page, Integer size) {
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        var records = noticeMapper.selectNotices(offset, normalizedSize);
        long total = noticeMapper.countNotices();
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    @Transactional
    public NoticeDTO create(Long publisherId, NoticeCreateRequest request) {
        Notice notice = toNotice(publisherId, request);
        noticeMapper.insertNotice(notice);
        messageService.notifyActiveUsers(
                MessageCodecs.TYPE_SYSTEM,
                "新公告：" + notice.getTitle(),
                notice.getContent(),
                publisherId
        );
        return noticeMapper.selectNoticeDetail(notice.getId());
    }

    @Transactional
    public NoticeDTO update(Long noticeId, NoticeCreateRequest request) {
        ValidateUtils.requirePositive(noticeId, "noticeId");
        if (noticeMapper.selectNoticeById(noticeId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        Notice notice = toNotice(null, request);
        notice.setId(noticeId);
        noticeMapper.updateNotice(notice);
        return noticeMapper.selectNoticeDetail(noticeId);
    }

    @Transactional
    public void delete(Long noticeId) {
        ValidateUtils.requirePositive(noticeId, "noticeId");
        int deleted = noticeMapper.deleteNotice(noticeId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
    }

    private Notice toNotice(Long publisherId, NoticeCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "公告内容不能为空");
        }
        String title = requiredTrim(request.title(), "公告标题不能为空");
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "公告标题不能超过 128 个字符");
        }
        String content = requiredTrim(request.content(), "公告正文不能为空");
        Notice notice = new Notice();
        notice.setPublisherId(publisherId);
        notice.setTitle(title);
        notice.setContent(content);
        return notice;
    }

    private String requiredTrim(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
