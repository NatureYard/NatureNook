package com.mcly.merchant.api;

public record MerchantTaskResponse(
        String type,
        String title,
        int count
) {
}

