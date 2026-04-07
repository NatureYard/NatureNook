package com.mcly.customer.api;

public record CustomerPassResponse(
        Long id,
        String name,
        String status,
        String storeName,
        String validFrom,
        String validTo,
        String reentryPolicy
) {
}
