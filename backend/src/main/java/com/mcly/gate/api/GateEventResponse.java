package com.mcly.gate.api;

public record GateEventResponse(
        Long id,
        String memberName,
        String direction,
        String result,
        String occurredAt
) {
}

