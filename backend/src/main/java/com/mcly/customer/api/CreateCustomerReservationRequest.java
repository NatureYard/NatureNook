package com.mcly.customer.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerReservationRequest(
        @NotBlank String ticketCode,
        @NotBlank String reservationDate,
        @NotBlank String timeSlot,
        @NotNull Long petId
) {
}
