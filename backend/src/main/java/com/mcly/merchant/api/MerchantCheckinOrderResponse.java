package com.mcly.merchant.api;

public record MerchantCheckinOrderResponse(
        Long orderId,
        String orderNo,
        Long memberId,
        String memberName,
        String petName,
        String orderType,
        String orderStatus,
        String storeName,
        String reservationDate,
        String timeSlot,
        boolean faceBound,
        boolean activeEntitlement,
        String entitlementName,
        String entitlementValidTo
) {
}
