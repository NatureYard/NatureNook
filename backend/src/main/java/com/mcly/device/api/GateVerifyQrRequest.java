package com.mcly.device.api;

import jakarta.validation.constraints.NotBlank;

public record GateVerifyQrRequest(
        @NotBlank String deviceCode,
        @NotBlank String qrContent,
        @NotBlank String direction
) {
}
