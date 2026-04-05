package com.mcly.boarding.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBoardingDailyRecordRequest(
        @NotNull Long boardingOrderId,
        @NotBlank String recordDate,
        String healthNote,
        String exceptionNote,
        Long staffId
) {}
