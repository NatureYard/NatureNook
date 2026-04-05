package com.mcly.material.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReportMaterialLossRequest(
        @NotNull Long storeId,
        @NotNull Long warehouseId,
        @NotNull Long staffId,
        @NotNull Long materialItemId,
        @NotNull @DecimalMin("0.01") BigDecimal quantity,
        @NotBlank String unit,
        String reason,
        String remark
) {
}
