package com.mcly.gate.api;

public record GateRuleResponse(
        String facePolicy,
        String sameDayReentryPolicy,
        String cardReentryPolicy,
        String antiFraudPolicy
) {
}

