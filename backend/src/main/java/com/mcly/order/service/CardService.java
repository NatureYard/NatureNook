package com.mcly.order.service;

import com.mcly.order.api.CardSummaryResponse;
import com.mcly.order.repository.CardQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    private final CardQueryRepository cardQueryRepository;

    public CardService(CardQueryRepository cardQueryRepository) {
        this.cardQueryRepository = cardQueryRepository;
    }

    public List<CardSummaryResponse> listCards() {
        return cardQueryRepository.listCards();
    }
}
