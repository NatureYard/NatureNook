package com.mcly.merchant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateManualReleaseRequest(
        @NotNull Long storeId,
        Long memberId,
        Long orderId,
        @NotNull Long staffId,
        @NotBlank String reason
) {
}

