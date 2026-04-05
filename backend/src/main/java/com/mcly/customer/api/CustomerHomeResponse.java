package com.mcly.customer.api;

import java.util.List;

public record CustomerHomeResponse(
        List<String> banners,
        List<String> quickEntries,
        String activeCardTip
) {
}

