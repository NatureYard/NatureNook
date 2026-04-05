package com.mcly.grooming.api;

import java.math.BigDecimal;

public record GroomingOrderResponse(
        Long id,
        String memberName,
        String petName,
        String staffName,
        String scheduledAt,
        String status,
        BigDecimal totalFee
) {}
