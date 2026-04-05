package com.mcly.material.api;

public record MaterialStockResponse(
        Long id,
        String name,
        String category,
        String unit,
        double quantity,
        double safetyStock,
        boolean warning
) {
}

