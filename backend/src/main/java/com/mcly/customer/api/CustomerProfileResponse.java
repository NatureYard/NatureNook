package com.mcly.customer.api;

import java.util.List;

public record CustomerProfileResponse(
        String memberName,
        String memberLevel,
        String storeName,
        List<String> items
) {
}
