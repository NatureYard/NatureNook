package com.mcly.dashboard.service;

import com.mcly.dashboard.api.DashboardSummaryResponse;
import com.mcly.dashboard.repository.DashboardQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    public DashboardService(DashboardQueryRepository dashboardQueryRepository) {
        this.dashboardQueryRepository = dashboardQueryRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return dashboardQueryRepository.getSummary();
    }
}
