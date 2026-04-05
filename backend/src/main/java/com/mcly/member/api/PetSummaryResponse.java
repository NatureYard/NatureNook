package com.mcly.member.api;

public record PetSummaryResponse(
        Long id,
        String name,
        String species,
        String breed,
        Long ownerId
) {
}

