package com.mcly.material.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.material.api.CreateMaterialIssueRequest;
import com.mcly.material.api.MaterialCategoryResponse;
import com.mcly.material.api.MaterialStockResponse;
import com.mcly.material.api.ReportMaterialLossRequest;
import com.mcly.material.service.MaterialService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<MaterialCategoryResponse>> categories() {
        return ApiResponse.ok(materialService.listCategories());
    }

    @GetMapping("/stocks")
    public ApiResponse<List<MaterialStockResponse>> stocks() {
        return ApiResponse.ok(materialService.listStocks());
    }

    @PostMapping("/issues")
    public ApiResponse<IdResponse> createIssue(@Valid @RequestBody CreateMaterialIssueRequest request) {
        return ApiResponse.ok(new IdResponse(materialService.createIssue(request)));
    }

    @PostMapping("/loss-orders")
    public ApiResponse<IdResponse> reportLoss(@Valid @RequestBody ReportMaterialLossRequest request) {
        return ApiResponse.ok(new IdResponse(materialService.reportLoss(request)));
    }
}
