package com.mcly.device.api;

public record GateVerifyResponse(
        Long memberId,
        boolean allowed,
        String reasonCode,
        boolean riskFlagged,
        boolean needManualReview
) {}
