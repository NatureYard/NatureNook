package com.mcly.customer.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.customer.api.CreateCustomerReservationRequest;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerReservationResponse;
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

    @GetMapping("/cards")
    public ApiResponse<List<CustomerCardResponse>> cards() {
        return ApiResponse.ok(customerMiniService.listCards());
    }

    @GetMapping("/pets")
    public ApiResponse<List<CustomerPetResponse>> pets() {
        return ApiResponse.ok(customerMiniService.listPets());
    }

    @GetMapping("/reservations")
    public ApiResponse<List<CustomerReservationResponse>> reservations() {
        return ApiResponse.ok(customerMiniService.listReservations());
    }

    @PostMapping("/reservations")
    public ApiResponse<IdResponse> createReservation(@Valid @RequestBody CreateCustomerReservationRequest request) {
        return ApiResponse.ok(new IdResponse(customerMiniService.createReservation(request)));
    }
}


