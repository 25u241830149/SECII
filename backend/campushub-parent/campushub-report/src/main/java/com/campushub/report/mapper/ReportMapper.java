package com.campushub.report.mapper;

import com.campushub.report.dto.ReportDTO;
import com.campushub.report.entity.Report;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportMapper {

    int insertReport(Report report);

    Report selectReportById(@Param("reportId") Long reportId);

    ReportDTO selectReportDetail(@Param("reportId") Long reportId);

    List<ReportDTO> selectReports(
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countReports(@Param("status") Integer status);

    int handleReport(
            @Param("reportId") Long reportId,
            @Param("handlerId") Long handlerId,
            @Param("status") Integer status,
            @Param("result") String result
    );
}
