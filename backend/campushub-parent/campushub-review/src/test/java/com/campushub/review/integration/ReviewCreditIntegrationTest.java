package com.campushub.review.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campushub.common.audit.SensitiveWordFilter;
import com.campushub.common.config.JwtConfig;
import com.campushub.common.security.JwtTokenProvider;
import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.campushub.common.testing.DatabaseFixtureHelper;
import com.campushub.order.service.OrderService;
import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDTO;
import com.campushub.review.service.CreditCalculator;
import com.campushub.review.service.ReviewService;
import com.campushub.task.service.TaskQueryService;
import com.campushub.task.service.TaskService;
import com.campushub.user.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = ReviewCreditIntegrationTest.TestApplication.class)
@ActiveProfiles("test")
class ReviewCreditIntegrationTest extends ContainerizedIntegrationTestSupport {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long posterId;
    private Long helperId;
    private Long orderId;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @BeforeEach
    void prepareData() {
        jdbcTemplate.execute("DELETE FROM t_review");
        jdbcTemplate.execute("DELETE FROM t_credit_record");
        jdbcTemplate.execute("DELETE FROM t_order");
        jdbcTemplate.execute("DELETE FROM t_task");
        jdbcTemplate.execute("DELETE FROM t_user_verification");
        jdbcTemplate.execute("DELETE FROM u_user");
        posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster", "Poster");
        helperId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "helper", "Helper");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Pickup task");
        orderId = DatabaseFixtureHelper.insertOrder(jdbcTemplate, taskId, posterId, helperId, 2);
    }

    @Test
    void createReviewWritesReviewAndCreditRecord() {
        ReviewDTO review = reviewService.create(posterId, new ReviewCreateRequest(orderId, helperId, 5, " Great job "));

        assertNotNull(review.reviewId());
        assertEquals(5, review.rating());
        assertEquals("Great job", review.content());

        Integer helperScore = jdbcTemplate.queryForObject(
                "SELECT credit_score FROM u_user WHERE id = ?",
                Integer.class,
                helperId
        );
        Long creditRecordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM t_credit_record WHERE user_id = ?",
                Long.class,
                helperId
        );
        assertEquals(93, helperScore);
        assertEquals(1L, creditRecordCount);
    }

    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = {
            ReviewService.class,
            CreditCalculator.class,
            CreditService.class,
            OrderService.class,
            TaskService.class,
            TaskQueryService.class,
            SensitiveWordFilter.class,
            JwtConfig.class,
            JwtTokenProvider.class
    })
    @MapperScan({
            "com.campushub.review.mapper",
            "com.campushub.order.mapper",
            "com.campushub.task.mapper",
            "com.campushub.user.mapper"
    })
    static class TestApplication {
    }
}
