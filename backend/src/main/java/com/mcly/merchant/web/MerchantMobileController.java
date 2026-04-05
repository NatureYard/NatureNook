package com.mcly.merchant.web;

import com.mcly.boarding.api.AddBoardingDailyRecordRequest;
import com.mcly.boarding.api.BoardingOrderResponse;
import com.mcly.boarding.api.CreateBoardingOrderRequest;
import com.mcly.boarding.service.BoardingService;
import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.grooming.api.CompleteGroomingRequest;
import com.mcly.grooming.api.CreateGroomingOrderRequest;
import com.mcly.grooming.api.GroomingOrderResponse;
import com.mcly.grooming.service.GroomingService;
import com.mcly.merchant.api.CreateManualReleaseRequest;
import com.mcly.merchant.api.MerchantTaskResponse;
import com.mcly.merchant.service.MerchantMobileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/m-app")
public class MerchantMobileController {

    private final MerchantMobileService merchantMobileService;
    private final BoardingService boardingService;
    private final GroomingService groomingService;

    public MerchantMobileController(
            MerchantMobileService merchantMobileService,
            BoardingService boardingService,
            GroomingService groomingService
    ) {
        this.merchantMobileService = merchantMobileService;
        this.boardingService = boardingService;
        this.groomingService = groomingService;
    }

    @GetMapping("/task-board")
    public ApiResponse<List<MerchantTaskResponse>> taskBoard() {
        return ApiResponse.ok(merchantMobileService.taskBoard());
    }

    @PostMapping("/manual-releases")
    public ApiResponse<IdResponse> createManualRelease(@Valid @RequestBody CreateManualReleaseRequest request) {
        return ApiResponse.ok(new IdResponse(merchantMobileService.createManualRelease(request)));
    }

    @GetMapping("/boarding/orders")
    public ApiResponse<List<BoardingOrderResponse>> listBoardingOrders() {
        return ApiResponse.ok(boardingService.listOrders());
    }

    @PostMapping("/boarding/orders")
    public ApiResponse<IdResponse> createBoardingOrder(@Valid @RequestBody CreateBoardingOrderRequest request) {
        return ApiResponse.ok(new IdResponse(boardingService.createOrder(request)));
    }

    @PostMapping("/boarding/daily-records")
    public ApiResponse<IdResponse> addBoardingDailyRecord(@Valid @RequestBody AddBoardingDailyRecordRequest request) {
        return ApiResponse.ok(new IdResponse(boardingService.addDailyRecord(request)));
    }

    @GetMapping("/grooming/orders")
    public ApiResponse<List<GroomingOrderResponse>> listGroomingOrders() {
        return ApiResponse.ok(groomingService.listOrders());
    }

    @PostMapping("/grooming/orders")
    public ApiResponse<IdResponse> createGroomingOrder(@Valid @RequestBody CreateGroomingOrderRequest request) {
        return ApiResponse.ok(new IdResponse(groomingService.createOrder(request)));
    }

    @PostMapping("/grooming/orders/complete")
    public ApiResponse<Void> completeGroomingOrder(@Valid @RequestBody CompleteGroomingRequest request) {
        groomingService.completeOrder(request);
        return ApiResponse.ok(null);
    }
}
