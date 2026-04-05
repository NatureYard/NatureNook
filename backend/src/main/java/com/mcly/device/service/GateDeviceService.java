package com.mcly.device.service;

import com.mcly.device.api.GateEventReportRequest;
import com.mcly.device.api.GateHeartbeatRequest;
import com.mcly.device.api.GateVerifyRequest;
import com.mcly.device.api.GateVerifyResponse;
import com.mcly.device.repository.GateDeviceCommandRepository;
import com.mcly.device.repository.GateDeviceQueryRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GateDeviceService {

    private final GateDeviceQueryRepository queryRepository;
    private final GateDeviceCommandRepository commandRepository;

    public GateDeviceService(
            GateDeviceQueryRepository queryRepository,
            GateDeviceCommandRepository commandRepository
    ) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
    }

    public GateVerifyResponse verifyPass(GateVerifyRequest request) {
        Map<String, Object> device = queryRepository.findDeviceByCode(request.deviceCode());
        if (device == null) {
            return new GateVerifyResponse(request.memberId(), false, "DEVICE_NOT_FOUND", false, true);
        }

        Long storeId = (Long) device.get("store_id");
        Long deviceId = (Long) device.get("id");

        if (queryRepository.isMemberBlacklisted(request.memberId())) {
            commandRepository.recordEntryExit(
                    request.memberId(), storeId, deviceId, request.direction(),
                    "BLOCKED", true, "BLACKLISTED");
            return new GateVerifyResponse(request.memberId(), false, "BLACKLISTED", true, false);
        }

        boolean hasEntitlement = queryRepository.hasActiveEntitlement(request.memberId(), storeId);
        if (!hasEntitlement) {
            commandRepository.recordEntryExit(
                    request.memberId(), storeId, deviceId, request.direction(),
                    "BLOCKED", false, "NO_ENTITLEMENT");
            return new GateVerifyResponse(request.memberId(), false, "NO_ENTITLEMENT", false, true);
        }

        commandRepository.recordEntryExit(
                request.memberId(), storeId, deviceId, request.direction(),
                "PASSED", false, null);
        return new GateVerifyResponse(request.memberId(), true, null, false, false);
    }

    public void reportEvent(GateEventReportRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), "ONLINE");
    }

    public void heartbeat(GateHeartbeatRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), request.status());
    }
}
