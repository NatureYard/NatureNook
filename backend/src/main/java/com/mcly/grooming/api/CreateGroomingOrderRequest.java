package com.mcly.grooming.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateGroomingOrderRequest(
        @NotNull Long storeId,
        @NotNull Long memberId,
        @NotNull Long petId,
        Long staffId,
        String scheduledAt,
        BigDecimal totalFee,
        String remark
) {}
