package com.mcly.order.api;

public record CardSummaryResponse(
        Long id,
        String memberName,
        String cardType,
        String status,
        String validTo
) {
}

