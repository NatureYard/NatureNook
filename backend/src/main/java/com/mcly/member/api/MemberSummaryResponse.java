package com.mcly.member.api;

public record MemberSummaryResponse(
        Long id,
        String name,
        String phone,
        String level,
        boolean faceBound
) {
}

