package com.mcly.customer.api;

public record WxLoginResponse(
        String token,
        Long memberId,
        String memberName,
        boolean isNewUser
) {
}
