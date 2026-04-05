package com.mcly.customer.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCustomerReservationRequest(
        @NotNull Long memberId,
        @NotNull Long storeId,
        @NotBlank String reservationType,
        @NotNull LocalDate reservationDate,
        @NotBlank String timeSlot,
        BigDecimal amount
) {
}
