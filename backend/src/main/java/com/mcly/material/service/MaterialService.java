package com.mcly.material.service;

import com.mcly.material.api.CreateMaterialIssueRequest;
import com.mcly.material.api.MaterialCategoryResponse;
import com.mcly.material.api.MaterialStockResponse;
import com.mcly.material.api.ReportMaterialLossRequest;
import com.mcly.material.repository.MaterialIssueCommandRepository;
import com.mcly.material.repository.MaterialLossCommandRepository;
import com.mcly.material.repository.MaterialQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialService {

    private final MaterialQueryRepository materialQueryRepository;
    private final MaterialIssueCommandRepository materialIssueCommandRepository;
    private final MaterialLossCommandRepository materialLossCommandRepository;

    public MaterialService(
            MaterialQueryRepository materialQueryRepository,
            MaterialIssueCommandRepository materialIssueCommandRepository,
            MaterialLossCommandRepository materialLossCommandRepository
    ) {
        this.materialQueryRepository = materialQueryRepository;
        this.materialIssueCommandRepository = materialIssueCommandRepository;
        this.materialLossCommandRepository = materialLossCommandRepository;
    }

    public List<MaterialCategoryResponse> listCategories() {
        return materialQueryRepository.listCategories();
    }

    public List<MaterialStockResponse> listStocks() {
        return materialQueryRepository.listStocks();
    }

    public Long createIssue(CreateMaterialIssueRequest request) {
        return materialIssueCommandRepository.create(request);
    }

    public Long reportLoss(ReportMaterialLossRequest request) {
        return materialLossCommandRepository.create(request);
    }
}
