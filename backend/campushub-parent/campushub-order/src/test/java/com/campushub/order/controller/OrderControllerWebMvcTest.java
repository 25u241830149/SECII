package com.campushub.order.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.GlobalExceptionHandler;
import com.campushub.common.response.PageResponse;
import com.campushub.common.security.SecurityUtils;
import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.dto.OrderStatsDTO;
import com.campushub.order.service.GrabService;
import com.campushub.order.service.OrderService;
import com.campushub.order.service.OrderStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private GrabService grabService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderStatusService orderStatusService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void grabRequiresAuthenticationContext() throws Exception {
        mockMvc.perform(post("/api/orders/grab")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new GrabOrderRequest(11L))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(grabService);
    }

    @Test
    void grabUsesCurrentUserAndWrapsDetail() throws Exception {
        authenticate(7L);
        GrabOrderRequest request = new GrabOrderRequest(11L);
        when(grabService.grab(eq(request), eq(7L))).thenReturn(orderDetail("PENDING"));

        mockMvc.perform(post("/api/orders/grab")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(31L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void confirmUsesCurrentUserAndReturnsWrappedDetail() throws Exception {
        authenticate(7L);
        when(orderStatusService.confirm(31L, 7L)).thenReturn(orderDetail("CONFIRMED"));

        mockMvc.perform(post("/api/orders/31/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void detailUsesCurrentUser() throws Exception {
        authenticate(7L);
        when(orderService.getDetail(31L, 7L)).thenReturn(orderDetail("PENDING"));

        mockMvc.perform(get("/api/orders/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(31L));
    }

    @Test
    void listWrapsPagedOrders() throws Exception {
        authenticate(7L);
        OrderListDTO row = new OrderListDTO(
                31L, 11L, "Need pickup", "EXPRESS", "OPEN", "Dorm", BigDecimal.TEN,
                "CONFIRMED", false, null, null, 7L, "Poster", null, 9L, "Helper", null,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
        when(orderService.list(7L, "helper", "CONFIRMED", 2, 5))
                .thenReturn(PageResponse.of(List.of(row), 1L, 2, 5));

        mockMvc.perform(get("/api/orders")
                        .param("userId", "7")
                        .param("role", "helper")
                        .param("status", "CONFIRMED")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].orderId").value(31L))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void statsWrapsSnapshot() throws Exception {
        authenticate(7L);
        when(orderService.stats(7L)).thenReturn(new OrderStatsDTO(1, 2, 3, 4, 5));

        mockMvc.perform(get("/api/orders/stats").param("userId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.waitingAcceptance").value(1))
                .andExpect(jsonPath("$.data.completed").value(5));
    }

    @Test
    void abandonUsesCurrentUser() throws Exception {
        authenticate(9L);
        when(orderStatusService.abandon(31L, 9L)).thenReturn(orderDetail("CANCELLED"));

        mockMvc.perform(post("/api/orders/31/abandon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    private static void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, UserRole.USER),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }

    private static OrderDetailDTO orderDetail(String status) {
        return new OrderDetailDTO(
                31L,
                11L,
                "Need pickup",
                "desc",
                "EXPRESS",
                "OPEN",
                "Dorm",
                BigDecimal.TEN,
                status,
                false,
                null,
                null,
                7L,
                "Poster",
                null,
                90,
                9L,
                "Helper",
                null,
                90,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00"),
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
