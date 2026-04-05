package com.mcly.risk.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.risk.api.RiskEventResponse;
import com.mcly.risk.service.RiskService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/events")
    public ApiResponse<List<RiskEventResponse>> listEvents() {
        return ApiResponse.ok(riskService.listOpenEvents());
    }
}
