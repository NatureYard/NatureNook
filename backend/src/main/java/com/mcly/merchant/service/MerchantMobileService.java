package com.mcly.merchant.service;

import com.mcly.merchant.api.CreateManualReleaseRequest;
import com.mcly.merchant.api.MerchantTaskResponse;
import com.mcly.merchant.repository.ManualReleaseCommandRepository;
import com.mcly.merchant.repository.MerchantTaskQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MerchantMobileService {

    private final MerchantTaskQueryRepository merchantTaskQueryRepository;
    private final ManualReleaseCommandRepository manualReleaseCommandRepository;

    public MerchantMobileService(
            MerchantTaskQueryRepository merchantTaskQueryRepository,
            ManualReleaseCommandRepository manualReleaseCommandRepository
    ) {
        this.merchantTaskQueryRepository = merchantTaskQueryRepository;
        this.manualReleaseCommandRepository = manualReleaseCommandRepository;
    }

    public List<MerchantTaskResponse> taskBoard() {
        return merchantTaskQueryRepository.taskBoard();
    }

    public Long createManualRelease(CreateManualReleaseRequest request) {
        return manualReleaseCommandRepository.create(request);
    }
}
