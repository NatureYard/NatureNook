package com.mcly.entrytoken.api;

import jakarta.validation.constraints.NotNull;

public record RiskReportRequest(
        @NotNull Long passEntitlementId,
        String reason
) {
}
