package com.mcly.customer.api;

import jakarta.validation.constraints.NotBlank;

public record PrepayRequest(
        @NotBlank(message = "订单号不能为空")
        String orderNo
) {
}
