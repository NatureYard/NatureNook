package com.mcly.device.api;

import jakarta.validation.constraints.NotBlank;

public record GateHeartbeatRequest(
        @NotBlank String deviceCode,
        String status
) {}
