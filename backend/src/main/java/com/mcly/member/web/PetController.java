package com.mcly.member.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.member.api.PetSummaryResponse;
import com.mcly.member.service.PetService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ApiResponse<List<PetSummaryResponse>> list() {
        return ApiResponse.ok(petService.listPets());
    }
}

