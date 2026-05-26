package com.campushub.report.dto;

import java.math.BigDecimal;

public record AdminDashboardStatsDTO(
        long totalUsers,
        long pendingVerifications,
        long bannedUsers,
        long openTasks,
        long inProgressTasks,
        long completedTasks,
        long totalOrders,
        long completedOrders,
        long pendingReports,
        long totalReports,
        long unreadMessages,
        long totalReviews,
        BigDecimal averageRating
) {
}
