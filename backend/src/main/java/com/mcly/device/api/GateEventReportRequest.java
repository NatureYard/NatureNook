package com.mcly.device.api;

import jakarta.validation.constraints.NotBlank;

public record GateEventReportRequest(
        @NotBlank String deviceCode,
        String eventType,
        String detail
) {}
