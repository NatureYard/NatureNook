package com.mcly.device.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.device.api.GateEventReportRequest;
import com.mcly.device.api.GateHeartbeatRequest;
import com.mcly.device.api.GateVerifyQrRequest;
import com.mcly.device.api.GateVerifyRequest;
import com.mcly.device.api.GateVerifyResponse;
import com.mcly.device.service.GateDeviceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device/gate")
public class GateDeviceController {

    private final GateDeviceService gateDeviceService;

    public GateDeviceController(GateDeviceService gateDeviceService) {
        this.gateDeviceService = gateDeviceService;
    }

    @PostMapping("/verify-pass")
    public ApiResponse<GateVerifyResponse> verifyPass(@Valid @RequestBody GateVerifyRequest request) {
        return ApiResponse.ok(gateDeviceService.verifyPass(request));
    }

    @PostMapping("/verify-qr")
    public ApiResponse<GateVerifyResponse> verifyQrPass(@Valid @RequestBody GateVerifyQrRequest request) {
        return ApiResponse.ok(gateDeviceService.verifyQrPass(request));
    }

    @PostMapping("/report-event")
    public ApiResponse<Void> reportEvent(@Valid @RequestBody GateEventReportRequest request) {
        gateDeviceService.reportEvent(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/heartbeat")
    public ApiResponse<Void> heartbeat(@Valid @RequestBody GateHeartbeatRequest request) {
        gateDeviceService.heartbeat(request);
        return ApiResponse.ok(null);
    }
}
