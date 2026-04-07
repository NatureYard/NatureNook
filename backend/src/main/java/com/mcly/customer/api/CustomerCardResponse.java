package com.mcly.customer.api;

public record CustomerCardResponse(
        Long id,
        String name,
        String desc,
        String price,
        String status,
        String validTo
) {
}
