package com.campushub.report.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.campushub.common.testing.DatabaseFixtureHelper;
import com.campushub.report.dto.AdminDashboardStatsDTO;
import com.campushub.report.dto.ReportCreateRequest;
import com.campushub.report.dto.ReportDTO;
import com.campushub.report.dto.ReportHandleRequest;
import com.campushub.report.mapper.AdminStatsMapper;
import com.campushub.report.mapper.ReportMapper;
import com.campushub.report.service.AdminStatsService;
import com.campushub.report.service.ReportService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = ReportFlowIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class ReportFlowIntegrationTest extends ContainerizedIntegrationTestSupport {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AdminStatsService adminStatsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM t_message");
        jdbcTemplate.execute("DELETE FROM t_report");
        jdbcTemplate.execute("DELETE FROM t_review");
        jdbcTemplate.execute("DELETE FROM t_user_verification");
        jdbcTemplate.execute("DELETE FROM t_order");
        jdbcTemplate.execute("DELETE FROM t_task");
        jdbcTemplate.execute("DELETE FROM u_user WHERE student_id <> 'admin'");
    }

    @Test
    void createAndHandleUserReportFlowPersists() {
        Long reporterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20262001", "Reporter");
        Long targetUserId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20262002", "Target");
        Long adminId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20262003", "Admin", 1, 0, 95);

        ReportDTO created = reportService.create(
                reporterId,
                new ReportCreateRequest("USER", targetUserId, null, "  abusive behavior  ")
        );

        assertEquals("PENDING", created.status());
        assertEquals(targetUserId, created.targetUserId());

        ReportDTO handled = reportService.handle(
                created.reportId(),
                adminId,
                new ReportHandleRequest("HANDLED", "Confirmed")
        );

        assertEquals("HANDLED", handled.status());
        assertEquals(adminId, handled.handlerId());
        assertEquals("Confirmed", handled.result());
        Integer persistedStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM t_report WHERE id = ?",
                Integer.class,
                created.reportId()
        );
        assertEquals(1, persistedStatus);
    }

    @Test
    void dashboardReflectsModerationCounts() {
        Long reporterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20262011", "Reporter");
        Long targetUserId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "20262012", "Target");
        DatabaseFixtureHelper.insertMessage(jdbcTemplate, targetUserId, 0, "Welcome", "Body", false);
        DatabaseFixtureHelper.insertReport(
                jdbcTemplate,
                reporterId,
                targetUserId,
                0,
                targetUserId,
                "Spam",
                0
        );

        AdminDashboardStatsDTO stats = adminStatsService.dashboard();

        assertEquals(3L, stats.totalUsers());
        assertEquals(1L, stats.pendingReports());
        assertEquals(1L, stats.totalReports());
        assertEquals(1L, stats.unreadMessages());
        assertEquals(BigDecimal.ZERO, stats.averageRating());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ReportService.class, AdminStatsService.class})
    @MapperScan(basePackageClasses = {ReportMapper.class, AdminStatsMapper.class})
    static class TestApplication {
    }
}
