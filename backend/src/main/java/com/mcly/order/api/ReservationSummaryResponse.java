package com.mcly.order.api;

public record ReservationSummaryResponse(
        Long id,
        String memberName,
        String storeName,
        String reservationType,
        String reservationDate,
        String timeSlot,
        String status
) {
}

