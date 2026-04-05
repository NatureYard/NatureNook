package com.mcly.merchant.api;

public record MerchantCheckinVerifyResponse(
        Long orderId,
        String orderNo,
        String memberName,
        boolean allowed,
        String reasonCode,
        boolean riskFlagged,
        boolean needManualReview
) {
}
