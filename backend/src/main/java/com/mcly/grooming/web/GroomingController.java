package com.mcly.grooming.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.grooming.api.CompleteGroomingRequest;
import com.mcly.grooming.api.CreateGroomingOrderRequest;
import com.mcly.grooming.api.GroomingOrderResponse;
import com.mcly.grooming.service.GroomingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/grooming")
public class GroomingController {

    private final GroomingService groomingService;

    public GroomingController(GroomingService groomingService) {
        this.groomingService = groomingService;
    }

    @GetMapping("/orders")
    public ApiResponse<List<GroomingOrderResponse>> listOrders() {
        return ApiResponse.ok(groomingService.listOrders());
    }

    @PostMapping("/orders")
    public ApiResponse<IdResponse> createOrder(@Valid @RequestBody CreateGroomingOrderRequest request) {
        return ApiResponse.ok(new IdResponse(groomingService.createOrder(request)));
    }

    @PostMapping("/orders/complete")
    public ApiResponse<Void> completeOrder(@Valid @RequestBody CompleteGroomingRequest request) {
        groomingService.completeOrder(request);
        return ApiResponse.ok(null);
    }
}
