package com.mcly.customer.api;

public record CustomerPetResponse(
        Long id,
        String name,
        String species,
        String breed,
        String gender,
        String ownerName
) {
}
