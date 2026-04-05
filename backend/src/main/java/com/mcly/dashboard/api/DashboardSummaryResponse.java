package com.mcly.dashboard.api;

public record DashboardSummaryResponse(
        long totalMembers,
        long activeCards,
        long todayReservations,
        long todayEntries,
        long openRiskEvents,
        long lowStockItems
) {
}

