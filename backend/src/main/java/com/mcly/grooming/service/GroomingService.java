package com.mcly.grooming.service;

import com.mcly.grooming.api.CompleteGroomingRequest;
import com.mcly.grooming.api.CreateGroomingOrderRequest;
import com.mcly.grooming.api.GroomingOrderResponse;
import com.mcly.grooming.repository.GroomingCommandRepository;
import com.mcly.grooming.repository.GroomingQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GroomingService {

    private final GroomingQueryRepository groomingQueryRepository;
    private final GroomingCommandRepository groomingCommandRepository;

    public GroomingService(
            GroomingQueryRepository groomingQueryRepository,
            GroomingCommandRepository groomingCommandRepository
    ) {
        this.groomingQueryRepository = groomingQueryRepository;
        this.groomingCommandRepository = groomingCommandRepository;
    }

    public List<GroomingOrderResponse> listOrders() {
        return groomingQueryRepository.listOrders();
    }

    public Long createOrder(CreateGroomingOrderRequest request) {
        return groomingCommandRepository.createOrder(request);
    }

    public void completeOrder(CompleteGroomingRequest request) {
        groomingCommandRepository.completeOrder(request);
    }
}
