package com.campushub.review.controller;

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
import com.campushub.review.dto.ReviewCreateRequest;
import com.campushub.review.dto.ReviewDTO;
import com.campushub.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ReviewControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRequiresAuthenticationContext() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(31L, 9L, 5, "Great");

        mockMvc.perform(post("/api/reviews")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verifyNoInteractions(reviewService);
    }

    @Test
    void createUsesCurrentUserAndWrapsResponse() throws Exception {
        authenticate(7L, UserRole.USER);
        ReviewCreateRequest request = new ReviewCreateRequest(31L, 9L, 5, "Great");
        when(reviewService.create(eq(7L), eq(request))).thenReturn(review(41L, 31L, 7L, 9L, 5, "Great"));

        mockMvc.perform(post("/api/reviews")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(41L))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    void byUserWrapsPagedReviews() throws Exception {
        ReviewDTO row = review(41L, 31L, 7L, 9L, 5, "Great");
        when(reviewService.listByUser(9L, 2, 5)).thenReturn(PageResponse.of(List.of(row), 1L, 2, 5));

        mockMvc.perform(get("/api/reviews/user/9")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].reviewId").value(41L))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void byOrderUsesCurrentUser() throws Exception {
        authenticate(7L, UserRole.USER);
        when(reviewService.listByOrder(31L, 7L)).thenReturn(List.of(review(41L, 31L, 7L, 9L, 5, "Great")));

        mockMvc.perform(get("/api/reviews/order/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].targetUserId").value(9L));
    }

    private static void authenticate(Long userId, UserRole role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new SecurityUtils.CurrentUser(userId, "user" + userId, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        ));
    }

    private static ReviewDTO review(
            Long reviewId,
            Long orderId,
            Long reviewerId,
            Long targetUserId,
            Integer rating,
            String content
    ) {
        return new ReviewDTO(
                reviewId,
                orderId,
                reviewerId,
                "Reviewer",
                null,
                targetUserId,
                "Target",
                null,
                rating,
                content,
                OffsetDateTime.parse("2026-06-05T12:00:00+08:00")
        );
    }
}
