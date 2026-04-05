package com.mcly.material.service;

import com.mcly.material.api.CreateMaterialIssueRequest;
import com.mcly.material.api.MaterialCategoryResponse;
import com.mcly.material.api.MaterialStockResponse;
import com.mcly.material.repository.MaterialIssueCommandRepository;
import com.mcly.material.repository.MaterialQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialService {

    private final MaterialQueryRepository materialQueryRepository;
    private final MaterialIssueCommandRepository materialIssueCommandRepository;

    public MaterialService(
            MaterialQueryRepository materialQueryRepository,
            MaterialIssueCommandRepository materialIssueCommandRepository
    ) {
        this.materialQueryRepository = materialQueryRepository;
        this.materialIssueCommandRepository = materialIssueCommandRepository;
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
}
