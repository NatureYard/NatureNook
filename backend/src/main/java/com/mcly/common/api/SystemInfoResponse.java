package com.mcly.common.api;

import java.util.List;

public record SystemInfoResponse(
        String systemName,
        String version,
        List<String> domains
) {
}

