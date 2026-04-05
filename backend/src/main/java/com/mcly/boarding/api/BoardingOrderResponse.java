package com.mcly.boarding.api;

import java.math.BigDecimal;

public record BoardingOrderResponse(
        Long id,
        String memberName,
        String petName,
        String cageNo,
        String checkInTime,
        String plannedCheckOutTime,
        String status,
        BigDecimal totalFee
) {}
