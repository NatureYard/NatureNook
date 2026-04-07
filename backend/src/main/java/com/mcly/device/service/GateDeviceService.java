package com.mcly.device.service;

import com.mcly.device.api.GateEventReportRequest;
import com.mcly.device.api.GateHeartbeatRequest;
import com.mcly.device.api.GateVerifyRequest;
import com.mcly.device.api.GateVerifyResponse;
import com.mcly.device.repository.GateDeviceCommandRepository;
import com.mcly.device.repository.GateDeviceQueryRepository;
import com.mcly.risk.repository.RiskEventCommandRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GateDeviceService {

    private final GateDeviceQueryRepository queryRepository;
    private final GateDeviceCommandRepository commandRepository;
    private final RiskEventCommandRepository riskEventCommandRepository;

    public GateDeviceService(
            GateDeviceQueryRepository queryRepository,
            GateDeviceCommandRepository commandRepository,
            RiskEventCommandRepository riskEventCommandRepository
    ) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
        this.riskEventCommandRepository = riskEventCommandRepository;
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
            riskEventCommandRepository.create(storeId, "BLACKLISTED_ENTRY_ATTEMPT", "HIGH", "MEMBER", request.memberId());
            return new GateVerifyResponse(request.memberId(), false, "BLACKLISTED", true, false);
        }

        boolean hasEntitlement = queryRepository.hasActiveEntitlement(request.memberId(), storeId);
        if (!hasEntitlement) {
            commandRepository.recordEntryExit(
                    request.memberId(), storeId, deviceId, request.direction(),
                    "BLOCKED", false, "NO_ENTITLEMENT");
            riskEventCommandRepository.create(storeId, "NO_ENTITLEMENT_ENTRY_ATTEMPT", "MEDIUM", "MEMBER", request.memberId());
            return new GateVerifyResponse(request.memberId(), false, "NO_ENTITLEMENT", false, true);
        }

        commandRepository.recordEntryExit(
                request.memberId(), storeId, deviceId, request.direction(),
                "PASSED", false, null);
        return new GateVerifyResponse(request.memberId(), true, null, false, false);
    }

    public void reportEvent(GateEventReportRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), "ONLINE");
        commandRepository.logGateEvent(request.deviceCode(), request.eventType(), request.detail());
    }

    public void heartbeat(GateHeartbeatRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), request.status());
    }
}
