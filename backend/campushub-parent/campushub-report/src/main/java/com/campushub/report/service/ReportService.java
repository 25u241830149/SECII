package com.campushub.report.service;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.event.ReportHandledEvent;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.utils.ValidateUtils;
import com.campushub.report.dto.ReportCreateRequest;
import com.campushub.report.dto.ReportDTO;
import com.campushub.report.dto.ReportHandleRequest;
import com.campushub.report.entity.Report;
import com.campushub.report.mapper.ReportMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private static final int MAX_REASON_LENGTH = 255;

    private final ReportMapper reportMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ReportService(ReportMapper reportMapper, ApplicationEventPublisher applicationEventPublisher) {
        this.reportMapper = reportMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public ReportDTO create(Long reporterId, ReportCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "举报内容不能为空");
        }
        ValidateUtils.requirePositive(request.targetId(), "targetId");
        int targetType = ReportCodecs.targetTypeCode(request.targetType());
        Long targetUserId = request.targetUserId();
        if (targetType == ReportCodecs.TARGET_USER && targetUserId == null) {
            targetUserId = request.targetId();
        }
        ValidateUtils.requirePositive(targetUserId, "targetUserId");
        String reason = requiredTrim(request.reason(), "举报原因不能为空");
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "举报原因不能超过 255 个字符");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetUserId(targetUserId);
        report.setTargetType(targetType);
        report.setTargetId(request.targetId());
        report.setReason(reason);
        report.setStatus(ReportCodecs.STATUS_PENDING);
        fillTargetReference(report, targetType, request.targetId());
        reportMapper.insertReport(report);
        return reportMapper.selectReportDetail(report.getId());
    }

    public PageResponse<ReportDTO> list(String status, Integer page, Integer size) {
        int normalizedPage = ValidateUtils.normalizePage(page);
        int normalizedSize = ValidateUtils.normalizePageSize(size);
        Integer statusCode = ReportCodecs.statusCode(status);
        int offset = (normalizedPage - 1) * normalizedSize;
        var records = reportMapper.selectReports(statusCode, offset, normalizedSize);
        long total = reportMapper.countReports(statusCode);
        return PageResponse.of(records, total, normalizedPage, normalizedSize);
    }

    @Transactional
    public ReportDTO handle(Long reportId, Long handlerId, ReportHandleRequest request) {
        ValidateUtils.requirePositive(reportId, "reportId");
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "处理结果不能为空");
        }
        Report existing = reportMapper.selectReportById(reportId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "举报不存在");
        }
        if (existing.getStatus() != null && existing.getStatus() != ReportCodecs.STATUS_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "该举报已经处理");
        }
        int status = ReportCodecs.handleStatusCode(request.status());
        String result = requiredTrim(request.result(), "处理说明不能为空");
        reportMapper.handleReport(reportId, handlerId, status, result);
        applicationEventPublisher.publishEvent(new ReportHandledEvent(
                existing.getId(),
                existing.getReporterId(),
                existing.getTargetUserId(),
                targetTypeName(existing.getTargetType()),
                existing.getTargetId(),
                status == ReportCodecs.STATUS_HANDLED ? "HANDLED" : "REJECTED",
                result
        ));
        return reportMapper.selectReportDetail(reportId);
    }

    private String targetTypeName(Integer targetType) {
        if (targetType == null) {
            return "UNKNOWN";
        }
        return switch (targetType) {
            case ReportCodecs.TARGET_USER -> "USER";
            case ReportCodecs.TARGET_TASK -> "TASK";
            case ReportCodecs.TARGET_ORDER -> "ORDER";
            case ReportCodecs.TARGET_POST -> "POST";
            case ReportCodecs.TARGET_COMMENT -> "COMMENT";
            default -> "UNKNOWN";
        };
    }

    private void fillTargetReference(Report report, int targetType, Long targetId) {
        switch (targetType) {
            case ReportCodecs.TARGET_TASK -> report.setTaskId(targetId);
            case ReportCodecs.TARGET_ORDER -> report.setOrderId(targetId);
            case ReportCodecs.TARGET_POST -> report.setPostId(targetId);
            case ReportCodecs.TARGET_COMMENT -> report.setCommentId(targetId);
            default -> {
            }
        }
    }

    private String requiredTrim(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
