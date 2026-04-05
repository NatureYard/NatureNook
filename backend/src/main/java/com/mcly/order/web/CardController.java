package com.mcly.order.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.order.api.CardSummaryResponse;
import com.mcly.order.service.CardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ApiResponse<List<CardSummaryResponse>> list() {
        return ApiResponse.ok(cardService.listCards());
    }
}

