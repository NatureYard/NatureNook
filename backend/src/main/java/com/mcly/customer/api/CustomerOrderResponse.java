package com.mcly.customer.api;

public record CustomerOrderResponse(
        Long id,
        String orderNo,
        String type,
        String status,
        String amount
) {
}

