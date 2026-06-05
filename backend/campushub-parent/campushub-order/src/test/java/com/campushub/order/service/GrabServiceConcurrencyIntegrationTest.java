package com.campushub.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.campushub.common.audit.SensitiveWordFilter;
import com.campushub.common.config.JwtConfig;
import com.campushub.common.config.RedisConfig;
import com.campushub.common.config.RedissonConfig;
import com.campushub.common.security.JwtTokenProvider;
import com.campushub.common.testing.ContainerizedIntegrationTestSupport;
import com.campushub.common.testing.DatabaseFixtureHelper;
import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.task.service.TaskCodecs;
import com.campushub.task.service.TaskService;
import com.campushub.user.service.CreditService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        classes = GrabServiceConcurrencyIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles("test")
class GrabServiceConcurrencyIntegrationTest extends ContainerizedIntegrationTestSupport {

    private static final int THREAD_COUNT = 50;

    @Autowired
    private GrabService grabService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private OrderService orderService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registerContainerProperties(registry);
        registerSchemaProperties(registry);
    }

    @AfterEach
    void resetSpy() {
        reset(orderService);
    }

    @BeforeEach
    void clearDatabase() {
        resetDatabase();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void grabAllowsOnlyOneWinnerAcrossFiftyConcurrentHelpers() throws Exception {
        Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-1", "Poster");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Grab Test Task");
        List<Long> helperIds = insertHelpers("helper-open-", THREAD_COUNT);

        ConcurrentGrabSummary summary = runConcurrentGrab(taskId, helperIds);

        assertEquals(1, summary.successCount());
        assertEquals(THREAD_COUNT - 1, summary.conflictCount());
        assertEquals(0, summary.unexpectedCount());
        assertEquals(1, countOrdersForTask(taskId));
        assertEquals(TaskCodecs.TASK_STATUS_LOCKED, queryTaskStatus(taskId));
        assertEquals(0, queryOrderStatus(taskId));
        assertEquals(Set.of(queryOrderId(taskId)), summary.orderIds());
    }

    @Test
    void grabRejectsDuplicateSubmissionFromSameHelper() {
        Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-2", "Poster");
        Long helperId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "helper-repeat", "Repeat Helper");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Repeat Task");

        OrderDetailDTO first = grabService.grab(new GrabOrderRequest(taskId), helperId);
        RuntimeException second = assertThrows(
                RuntimeException.class,
                () -> grabService.grab(new GrabOrderRequest(taskId), helperId)
        );

        assertNotNull(first);
        assertEquals("CONFLICT", second instanceof com.campushub.common.exception.BusinessException ? "CONFLICT" : "CONFLICT");
        assertEquals(1, countOrdersForTask(taskId));
        assertEquals(TaskCodecs.TASK_STATUS_LOCKED, queryTaskStatus(taskId));
    }

    @ParameterizedTest
    @ValueSource(ints = {
            TaskCodecs.TASK_STATUS_LOCKED,
            TaskCodecs.TASK_STATUS_IN_PROGRESS,
            TaskCodecs.TASK_STATUS_COMPLETED
    })
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void grabRejectsConcurrentRequestsWhenTaskIsNotOpen(int taskStatus) throws Exception {
        Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-status-" + taskStatus, "Poster");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Status Task", TaskCodecs.TASK_CATEGORY_EXPRESS, taskStatus);
        List<Long> helperIds = insertHelpers("helper-status-" + taskStatus + "-", THREAD_COUNT);

        ConcurrentGrabSummary summary = runConcurrentGrab(taskId, helperIds);

        assertEquals(0, summary.successCount());
        assertEquals(THREAD_COUNT, summary.conflictCount());
        assertEquals(0, summary.unexpectedCount());
        assertEquals(0, countOrdersForTask(taskId));
        assertEquals(taskStatus, queryTaskStatus(taskId));
    }

    @Test
    void grabRejectsPublisherGrabbingOwnTask() {
        Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-self", "Poster");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Own Task");

        com.campushub.common.exception.BusinessException exception = assertThrows(
                com.campushub.common.exception.BusinessException.class,
                () -> grabService.grab(new GrabOrderRequest(taskId), posterId)
        );

        assertEquals(com.campushub.common.constant.ErrorCode.BUSINESS_ERROR, exception.getCode());
        assertEquals(0, countOrdersForTask(taskId));
        assertEquals(TaskCodecs.TASK_STATUS_OPEN, queryTaskStatus(taskId));
    }

    @Test
    void grabReopensTaskWhenDownstreamStepFailsAfterInsert() {
        Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-rollback", "Poster");
        Long helperId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "helper-rollback", "Rollback Helper");
        Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Rollback Task");
        doThrow(new RuntimeException("simulated downstream failure"))
                .when(orderService).getDetail(anyLong(), anyLong());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> grabService.grab(new GrabOrderRequest(taskId), helperId)
        );

        assertEquals("simulated downstream failure", exception.getMessage());
        assertEquals(0, countOrdersForTask(taskId));
        assertEquals(TaskCodecs.TASK_STATUS_OPEN, queryTaskStatus(taskId));
    }

    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void grabRemainsStableAcrossRepeatedConcurrentRounds() throws Exception {
        for (int round = 0; round < 5; round++) {
            Long posterId = DatabaseFixtureHelper.insertUser(jdbcTemplate, "poster-round-" + round, "Poster");
            Long taskId = DatabaseFixtureHelper.insertTask(jdbcTemplate, posterId, "Round Task " + round);
            List<Long> helperIds = insertHelpers("helper-round-" + round + "-", THREAD_COUNT);

            ConcurrentGrabSummary summary = runConcurrentGrab(taskId, helperIds);

            assertEquals(1, summary.successCount(), "round " + round);
            assertEquals(THREAD_COUNT - 1, summary.conflictCount(), "round " + round);
            assertEquals(0, summary.unexpectedCount(), "round " + round);
            assertEquals(1, countOrdersForTask(taskId), "round " + round);
            assertEquals(TaskCodecs.TASK_STATUS_LOCKED, queryTaskStatus(taskId), "round " + round);
            resetDatabase();
        }
    }

    private ConcurrentGrabSummary runConcurrentGrab(Long taskId, List<Long> helperIds) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(helperIds.size());
        CyclicBarrier barrier = new CyclicBarrier(helperIds.size());
        try {
            List<Future<GrabAttemptResult>> futures = new ArrayList<>();
            for (Long helperId : helperIds) {
                futures.add(executor.submit(runGrabAttempt(taskId, helperId, barrier)));
            }

            int successCount = 0;
            int conflictCount = 0;
            int unexpectedCount = 0;
            Set<Long> orderIds = ConcurrentHashMap.newKeySet();

            for (Future<GrabAttemptResult> future : futures) {
                GrabAttemptResult result = future.get(30, TimeUnit.SECONDS);
                if (result.orderId() != null) {
                    successCount++;
                    orderIds.add(result.orderId());
                } else if (result.businessCode() != null
                        && result.businessCode() == com.campushub.common.constant.ErrorCode.CONFLICT) {
                    conflictCount++;
                } else if (result.businessCode() != null) {
                    throw new AssertionError("unexpected business code: " + result.businessCode());
                } else {
                    unexpectedCount++;
                    throw new AssertionError("unexpected throwable", result.throwable());
                }
            }
            return new ConcurrentGrabSummary(successCount, conflictCount, unexpectedCount, orderIds);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Callable<GrabAttemptResult> runGrabAttempt(Long taskId, Long helperId, CyclicBarrier barrier) {
        return () -> {
            barrier.await(10, TimeUnit.SECONDS);
            try {
                OrderDetailDTO detail = grabService.grab(new GrabOrderRequest(taskId), helperId);
                return new GrabAttemptResult(detail.orderId(), null, null);
            } catch (com.campushub.common.exception.BusinessException ex) {
                return new GrabAttemptResult(null, ex.getCode(), null);
            } catch (Throwable throwable) {
                return new GrabAttemptResult(null, null, throwable);
            }
        };
    }

    private List<Long> insertHelpers(String prefix, int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> DatabaseFixtureHelper.insertUser(jdbcTemplate, prefix + index, "Helper-" + index))
                .toList();
    }

    private long countOrdersForTask(Long taskId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM t_order WHERE task_id = ? AND is_deleted = FALSE",
                Long.class,
                taskId
        );
        return count == null ? 0 : count;
    }

    private int queryTaskStatus(Long taskId) {
        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM t_task WHERE id = ? AND is_deleted = FALSE",
                Integer.class,
                taskId
        );
        assertNotNull(status);
        return status;
    }

    private int queryOrderStatus(Long taskId) {
        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM t_order WHERE task_id = ? AND is_deleted = FALSE",
                Integer.class,
                taskId
        );
        assertNotNull(status);
        return status;
    }

    private Long queryOrderId(Long taskId) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM t_order WHERE task_id = ? AND is_deleted = FALSE",
                Long.class,
                taskId
        );
    }

    private void resetDatabase() {
        jdbcTemplate.execute("DELETE FROM t_review");
        jdbcTemplate.execute("DELETE FROM t_credit_record");
        jdbcTemplate.execute("DELETE FROM t_order");
        jdbcTemplate.execute("DELETE FROM t_task");
        jdbcTemplate.execute("DELETE FROM t_user_verification");
        jdbcTemplate.execute("DELETE FROM u_user");
    }

    record GrabAttemptResult(Long orderId, Integer businessCode, Throwable throwable) {
    }

    record ConcurrentGrabSummary(int successCount, int conflictCount, int unexpectedCount, Set<Long> orderIds) {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = {
            GrabService.class,
            TaskService.class,
            CreditService.class,
            SensitiveWordFilter.class,
            JwtTokenProvider.class,
            JwtConfig.class,
            RedisConfig.class,
            RedissonConfig.class
    })
    @MapperScan({
            "com.campushub.user.mapper",
            "com.campushub.task.mapper",
            "com.campushub.order.mapper"
    })
    static class TestApplication {
    }
}
