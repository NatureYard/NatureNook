package com.mcly.gate.service;

import com.mcly.gate.api.GateEventResponse;
import com.mcly.gate.api.GateRuleResponse;
import com.mcly.gate.repository.GateQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GateService {

    private final GateQueryRepository gateQueryRepository;

    public GateService(GateQueryRepository gateQueryRepository) {
        this.gateQueryRepository = gateQueryRepository;
    }

    public GateRuleResponse getRules() {
        return new GateRuleResponse(
                "FACE_BINDING_REQUIRED",
                "SAME_DAY_UNLIMITED_REENTRY",
                "CARD_VALIDITY_BASED",
                "AUDIT_AND_RISK_ALERT"
        );
    }

    public List<GateEventResponse> listLatestEvents() {
        return gateQueryRepository.listLatestEvents();
    }
}
