package com.mcly.customer.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerContextResponse;
import com.mcly.customer.api.CreateCustomerReservationRequest;
import com.mcly.customer.api.CreateCustomerReservationResponse;
import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPassResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerProfileResponse;
import com.mcly.customer.api.CustomerTicketResponse;
import com.mcly.customer.api.PrepayRequest;
import com.mcly.customer.api.PrepayResponse;
import com.mcly.customer.service.CustomerMiniService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/context")
    public ApiResponse<CustomerContextResponse> context() {
        return ApiResponse.ok(customerMiniService.context());
    }

    @GetMapping("/pets")
    public ApiResponse<List<CustomerPetResponse>> pets() {
        return ApiResponse.ok(customerMiniService.pets());
    }

    @GetMapping("/passes")
    public ApiResponse<List<CustomerPassResponse>> passes() {
        return ApiResponse.ok(customerMiniService.passes());
    }

    @GetMapping("/cards")
    public ApiResponse<List<CustomerCardResponse>> cards() {
        return ApiResponse.ok(customerMiniService.cards());
    }

    @GetMapping("/profile")
    public ApiResponse<CustomerProfileResponse> profile() {
        return ApiResponse.ok(customerMiniService.profile());
    }

    @GetMapping("/tickets")
    public ApiResponse<List<CustomerTicketResponse>> tickets() {
        return ApiResponse.ok(customerMiniService.tickets());
    }

    @PostMapping("/reservations")
    public ApiResponse<CreateCustomerReservationResponse> createReservation(
            @Valid @RequestBody CreateCustomerReservationRequest request
    ) {
        return ApiResponse.ok(customerMiniService.createReservation(request));
    }

    /**
     * 预支付：根据订单号生成微信支付参数。
     * 小程序端拿到参数后调用 wx.requestPayment() 发起支付。
     */
    @PostMapping("/prepay")
    public ApiResponse<PrepayResponse> prepay(@Valid @RequestBody PrepayRequest request) {
        return ApiResponse.ok(customerMiniService.prepay(request.orderNo()));
    }

    /**
     * 确认支付（开发模式使用，正式环境由微信回调触发）。
     * 将订单从 PENDING_PAY 更新为 PAID 并生成通行资格。
     */
    @PostMapping("/payment/confirm")
    public ApiResponse<Void> confirmPayment(@RequestBody PrepayRequest request) {
        customerMiniService.confirmPayment(request.orderNo());
        return ApiResponse.ok(null);
    }
}
