package com.mcly.gate.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.gate.api.GateEventResponse;
import com.mcly.gate.api.GateRuleResponse;
import com.mcly.gate.service.GateService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/gates")
public class GateController {

    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @GetMapping("/rules")
    public ApiResponse<GateRuleResponse> rules() {
        return ApiResponse.ok(gateService.getRules());
    }

    @GetMapping("/events")
    public ApiResponse<List<GateEventResponse>> events() {
        return ApiResponse.ok(gateService.listLatestEvents());
    }
}

