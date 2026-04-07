package com.mcly.grooming.api;

import jakarta.validation.constraints.NotNull;

public record CompleteGroomingRequest(
        @NotNull Long orderId,
        String note
) {}
