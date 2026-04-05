package com.mcly.boarding.web;

import com.mcly.boarding.api.AddBoardingDailyRecordRequest;
import com.mcly.boarding.api.BoardingOrderResponse;
import com.mcly.boarding.api.CreateBoardingOrderRequest;
import com.mcly.boarding.service.BoardingService;
import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/boarding")
public class BoardingController {

    private final BoardingService boardingService;

    public BoardingController(BoardingService boardingService) {
        this.boardingService = boardingService;
    }

    @GetMapping("/orders")
    public ApiResponse<List<BoardingOrderResponse>> listOrders() {
        return ApiResponse.ok(boardingService.listOrders());
    }

    @PostMapping("/orders")
    public ApiResponse<IdResponse> createOrder(@Valid @RequestBody CreateBoardingOrderRequest request) {
        return ApiResponse.ok(new IdResponse(boardingService.createOrder(request)));
    }

    @PostMapping("/daily-records")
    public ApiResponse<IdResponse> addDailyRecord(@Valid @RequestBody AddBoardingDailyRecordRequest request) {
        return ApiResponse.ok(new IdResponse(boardingService.addDailyRecord(request)));
    }
}
