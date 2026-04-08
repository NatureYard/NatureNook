package com.mcly.entrytoken.api;

public record QrCodeGenerateResponse(
        String qrContent,
        String expiresAt,
        Long passEntitlementId,
        String passName,
        String storeName
) {
}
