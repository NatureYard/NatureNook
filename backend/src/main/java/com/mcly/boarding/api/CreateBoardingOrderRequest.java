package com.mcly.boarding.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBoardingOrderRequest(
        @NotNull Long storeId,
        @NotNull Long memberId,
        @NotNull Long petId,
        String cageNo,
        @NotBlank String checkInTime,
        @NotBlank String plannedCheckOutTime,
        String remark
) {}
