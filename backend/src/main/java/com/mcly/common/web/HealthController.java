package com.mcly.common.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.SystemInfoResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class HealthController {

    @GetMapping("/info")
    public ApiResponse<SystemInfoResponse> info() {
        return ApiResponse.ok(new SystemInfoResponse(
                "萌宠乐园管理系统",
                "0.0.1-SNAPSHOT",
                List.of("admin-web", "customer-mini", "merchant-mobile", "gate-adapter")
        ));
    }
}

