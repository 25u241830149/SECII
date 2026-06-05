package com.campushub.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.dto.OrderStatsDTO;
import com.campushub.order.entity.Order;
import com.campushub.order.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireOrderRejectsMissingOrder() {
        when(orderMapper.selectOrderById(31L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.requireOrder(31L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
    }

    @Test
    void getDetailRejectsUserOutsideOrder() {
        when(orderMapper.selectOrderById(31L)).thenReturn(order(31L, 7L, 9L, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.getDetail(31L, 15L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    @Test
    void getDetailAllowsAdminAccess() {
        authenticate(1L, UserRole.ADMIN);
        Order order = order(31L, 7L, 9L, 0);
        OrderDetailDTO detail = detail("PENDING");
        when(orderMapper.selectOrderById(31L)).thenReturn(order);
        when(orderMapper.selectOrderDetail(31L, 15L)).thenReturn(detail);

        OrderDetailDTO result = orderService.getDetail(31L, 15L);

        assertEquals(detail, result);
    }

    @Test
    void listNormalizesFiltersAndReturnsPage() {
        authenticate(7L, UserRole.USER);
        OrderListDTO row = listRow("CONFIRMED");
        when(orderMapper.selectOrderList(eq(7L), eq("helper"), eq("WAITING_REVIEW"), eq(10), eq(5)))
                .thenReturn(List.of(row));
        when(orderMapper.countOrderList(7L, "helper", "WAITING_REVIEW")).thenReturn(1L);

        PageResponse<OrderListDTO> page = orderService.list(7L, "helper", "WAITING_REVIEW", 3, 5);

        assertEquals(1L, page.total());
        assertEquals(3, page.page());
        assertEquals(row, page.records().get(0));
    }

    @Test
    void statsRequiresCurrentUserAndReturnsSnapshot() {
        authenticate(7L, UserRole.USER);
        OrderStatsDTO stats = new OrderStatsDTO(1, 2, 3, 4, 5);
        when(orderMapper.selectOrderStats(7L)).thenReturn(stats);

        assertEquals(stats, orderService.stats(7L));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static Order order(Long orderId, Long posterId, Long helperId, Integer status) {
        Order order = new Order();
        order.setId(orderId);
        order.setPosterId(posterId);
        order.setHelperId(helperId);
        order.setStatus(status);
        order.setVersion(0);
        order.setTaskId(11L);
        return order;
    }

    private static OrderDetailDTO detail(String status) {
        return new OrderDetailDTO(
                31L, 11L, "Need pickup", "desc", "EXPRESS", "OPEN", "Dorm", BigDecimal.TEN,
                status, false, null, null, 7L, "Poster", null, 90, 9L, "Helper", null, 90,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }

    private static OrderListDTO listRow(String status) {
        return new OrderListDTO(
                31L, 11L, "Need pickup", "EXPRESS", "OPEN", "Dorm", BigDecimal.TEN, status, false,
                null, null, 7L, "Poster", null, 9L, "Helper", null,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
