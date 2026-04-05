package com.mcly.risk.service;

import com.mcly.risk.api.RiskEventResponse;
import com.mcly.risk.repository.RiskQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final RiskQueryRepository riskQueryRepository;

    public RiskService(RiskQueryRepository riskQueryRepository) {
        this.riskQueryRepository = riskQueryRepository;
    }

    public List<RiskEventResponse> listOpenEvents() {
        return riskQueryRepository.listOpenEvents();
    }
}
