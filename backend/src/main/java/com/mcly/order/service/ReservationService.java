package com.mcly.order.service;

import com.mcly.order.api.CreateReservationRequest;
import com.mcly.order.api.ReservationSummaryResponse;
import com.mcly.order.repository.ReservationCommandRepository;
import com.mcly.order.repository.ReservationQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationCommandRepository reservationCommandRepository;

    public ReservationService(
            ReservationQueryRepository reservationQueryRepository,
            ReservationCommandRepository reservationCommandRepository
    ) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.reservationCommandRepository = reservationCommandRepository;
    }

    public List<ReservationSummaryResponse> listReservations() {
        return reservationQueryRepository.listReservations();
    }

    public Long createReservation(CreateReservationRequest request) {
        return reservationCommandRepository.create(request);
    }
}
