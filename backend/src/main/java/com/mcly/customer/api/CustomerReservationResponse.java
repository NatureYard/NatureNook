package com.mcly.customer.api;

public record CustomerReservationResponse(
        Long id,
        String memberName,
        String storeName,
        String reservationType,
        String reservationDate,
        String timeSlot,
        String status,
        String amount
) {
}
