package com.mcly.device.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GateVerifyRequest(
        @NotBlank String deviceCode,
        @NotNull Long memberId,
        @NotBlank String direction
) {}
