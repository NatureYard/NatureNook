package com.mcly.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateReservationRequest(
        @NotNull Long memberId,
        @NotNull Long storeId,
        @NotBlank String reservationType,
        @NotBlank String reservationDate,
        @NotBlank String timeSlot,
        @NotBlank String status,
        @NotNull @DecimalMin("0.00") BigDecimal amount
) {
}

