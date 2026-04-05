package com.mcly.customer.api;

public record CustomerTicketResponse(
        String code,
        String name,
        String desc,
        String price,
        String type
) {
}
