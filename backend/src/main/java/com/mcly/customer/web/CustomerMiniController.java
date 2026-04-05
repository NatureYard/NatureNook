package com.mcly.customer.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.service.CustomerMiniService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/c-app")
public class CustomerMiniController {

    private final CustomerMiniService customerMiniService;

    public CustomerMiniController(CustomerMiniService customerMiniService) {
        this.customerMiniService = customerMiniService;
    }

    @GetMapping("/home")
    public ApiResponse<CustomerHomeResponse> home() {
        return ApiResponse.ok(customerMiniService.home());
    }

    @GetMapping("/orders")
    public ApiResponse<List<CustomerOrderResponse>> orders() {
        return ApiResponse.ok(customerMiniService.orders());
    }
}

