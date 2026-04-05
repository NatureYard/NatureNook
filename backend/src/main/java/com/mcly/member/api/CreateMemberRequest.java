package com.mcly.member.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMemberRequest(
        @NotNull Long storeId,
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String level
) {
}

