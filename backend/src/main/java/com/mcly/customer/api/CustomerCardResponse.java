package com.mcly.customer.api;

public record CustomerCardResponse(
        Long id,
        String memberName,
        String cardType,
        String storeName,
        String status,
        String validFrom,
        String validTo
) {
}
