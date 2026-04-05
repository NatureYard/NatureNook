package com.mcly.order.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.order.api.CreateReservationRequest;
import com.mcly.order.api.ReservationSummaryResponse;
import com.mcly.order.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<List<ReservationSummaryResponse>> list() {
        return ApiResponse.ok(reservationService.listReservations());
    }

    @PostMapping
    public ApiResponse<IdResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        return ApiResponse.ok(new IdResponse(reservationService.createReservation(request)));
    }
}
