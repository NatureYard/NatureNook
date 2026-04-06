package com.mcly.customer.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.customer.api.WxLoginRequest;
import com.mcly.customer.api.WxLoginResponse;
import com.mcly.customer.service.WxAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/c-app")
public class CustomerAuthController {

    private final WxAuthService wxAuthService;

    public CustomerAuthController(WxAuthService wxAuthService) {
        this.wxAuthService = wxAuthService;
    }

    /**
     * 微信小程序登录。
     * 接收 wx.login() 返回的 code，换取 openid 后签发 token。
     */
    @PostMapping("/login")
    public ApiResponse<WxLoginResponse> login(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.ok(wxAuthService.login(request));
    }
}
