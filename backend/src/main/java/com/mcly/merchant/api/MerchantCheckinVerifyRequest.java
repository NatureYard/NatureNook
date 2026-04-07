package com.mcly.merchant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MerchantCheckinVerifyRequest(
        @NotNull Long orderId,
        @NotBlank String deviceCode,
        @NotBlank String direction
) {
}
