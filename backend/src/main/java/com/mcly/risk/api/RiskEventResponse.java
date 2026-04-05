package com.mcly.risk.api;

public record RiskEventResponse(
        Long id,
        String storeName,
        String eventType,
        String eventLevel,
        String subjectType,
        Long subjectId,
        String content,
        String status,
        String createdAt
) {}
