package com.mcly.boarding.service;

import com.mcly.boarding.api.AddBoardingDailyRecordRequest;
import com.mcly.boarding.api.BoardingOrderResponse;
import com.mcly.boarding.api.CreateBoardingOrderRequest;
import com.mcly.boarding.repository.BoardingCommandRepository;
import com.mcly.boarding.repository.BoardingQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BoardingService {

    private final BoardingQueryRepository boardingQueryRepository;
    private final BoardingCommandRepository boardingCommandRepository;

    public BoardingService(
            BoardingQueryRepository boardingQueryRepository,
            BoardingCommandRepository boardingCommandRepository
    ) {
        this.boardingQueryRepository = boardingQueryRepository;
        this.boardingCommandRepository = boardingCommandRepository;
    }

    public List<BoardingOrderResponse> listOrders() {
        return boardingQueryRepository.listOrders();
    }

    public Long createOrder(CreateBoardingOrderRequest request) {
        return boardingCommandRepository.createOrder(request);
    }

    public Long addDailyRecord(AddBoardingDailyRecordRequest request) {
        return boardingCommandRepository.addDailyRecord(request);
    }
}
