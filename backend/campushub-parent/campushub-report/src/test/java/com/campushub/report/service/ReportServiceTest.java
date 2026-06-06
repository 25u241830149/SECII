package com.campushub.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.exception.BusinessException;
import com.campushub.report.dto.ReportCreateRequest;
import com.campushub.report.dto.ReportDTO;
import com.campushub.report.dto.ReportHandleRequest;
import com.campushub.report.entity.Report;
import com.campushub.report.mapper.ReportMapper;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReportService reportService;

    @Test
    void createDefaultsTargetUserForUserReports() {
        when(reportMapper.insertReport(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(91L);
            return 1;
        });
        ReportDTO detail = reportDetail(91L, 7L, 9L, "PENDING", null, null);
        when(reportMapper.selectReportDetail(91L)).thenReturn(detail);

        ReportDTO result = reportService.create(7L, new ReportCreateRequest("USER", 9L, null, "  abusive behavior  "));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportMapper).insertReport(captor.capture());
        Report persisted = captor.getValue();
        assertEquals(7L, persisted.getReporterId());
        assertEquals(9L, persisted.getTargetUserId());
        assertEquals(ReportCodecs.TARGET_USER, persisted.getTargetType());
        assertEquals(ReportCodecs.STATUS_PENDING, persisted.getStatus());
        assertEquals("abusive behavior", persisted.getReason());
        assertEquals(detail, result);
    }

    @Test
    void createRejectsTooLongReason() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reportService.create(7L, new ReportCreateRequest("USER", 9L, null, "x".repeat(256)))
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
    }

    @Test
    void handleRejectsAlreadyProcessedReport() {
        Report report = new Report();
        report.setId(91L);
        report.setStatus(ReportCodecs.STATUS_HANDLED);
        when(reportMapper.selectReportById(91L)).thenReturn(report);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reportService.handle(91L, 1L, new ReportHandleRequest("HANDLED", "resolved"))
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
    }

    @Test
    void handleUpdatesPendingReportAndReturnsDetail() {
        Report report = new Report();
        report.setId(91L);
        report.setStatus(ReportCodecs.STATUS_PENDING);
        when(reportMapper.selectReportById(91L)).thenReturn(report);
        ReportDTO detail = reportDetail(91L, 7L, 9L, "HANDLED", 1L, "Admin");
        when(reportMapper.selectReportDetail(91L)).thenReturn(detail);

        ReportDTO result = reportService.handle(91L, 1L, new ReportHandleRequest("HANDLED", "  resolved  "));

        verify(reportMapper).handleReport(91L, 1L, ReportCodecs.STATUS_HANDLED, "resolved");
        assertEquals(detail, result);
    }

    private static ReportDTO reportDetail(
            Long reportId,
            Long reporterId,
            Long targetUserId,
            String status,
            Long handlerId,
            String handlerName
    ) {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-05T12:00:00+08:00");
        return new ReportDTO(
                reportId,
                reporterId,
                "Reporter",
                targetUserId,
                "Target",
                handlerId,
                handlerName,
                null,
                null,
                null,
                null,
                "USER",
                targetUserId,
                "abusive behavior",
                status,
                handlerId == null ? null : "resolved",
                now,
                now
        );
    }
}
