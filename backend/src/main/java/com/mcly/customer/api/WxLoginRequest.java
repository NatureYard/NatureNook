package com.mcly.customer.api;

import jakarta.validation.constraints.NotBlank;

public record WxLoginRequest(
        @NotBlank(message = "微信登录 code 不能为空")
        String code
) {
}
