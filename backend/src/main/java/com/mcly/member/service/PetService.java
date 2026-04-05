package com.mcly.member.service;

import com.mcly.member.api.PetSummaryResponse;
import com.mcly.member.repository.PetQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    private final PetQueryRepository petQueryRepository;

    public PetService(PetQueryRepository petQueryRepository) {
        this.petQueryRepository = petQueryRepository;
    }

    public List<PetSummaryResponse> listPets() {
        return petQueryRepository.listPets();
    }
}
