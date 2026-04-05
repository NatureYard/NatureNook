package com.mcly.customer.api;

import java.util.List;

public record CustomerContextResponse(
        Long memberId,
        String memberName,
        String memberLevel,
        Long storeId,
        String storeName,
        List<CustomerPetResponse> pets,
        List<CustomerCardResponse> cards
) {
}
