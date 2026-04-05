package com.mcly.customer.api;

public record CreateCustomerReservationResponse(
        Long reservationId,
        Long orderId,
        String orderNo,
        String status
) {
}
