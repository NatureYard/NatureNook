package com.mcly.merchant.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.merchant.api.CreateManualReleaseRequest;
import com.mcly.merchant.api.MerchantTaskResponse;
import com.mcly.merchant.service.MerchantMobileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/m-app")
public class MerchantMobileController {

    private final MerchantMobileService merchantMobileService;

    public MerchantMobileController(MerchantMobileService merchantMobileService) {
        this.merchantMobileService = merchantMobileService;
    }

    @GetMapping("/task-board")
    public ApiResponse<List<MerchantTaskResponse>> taskBoard() {
        return ApiResponse.ok(merchantMobileService.taskBoard());
    }

    @PostMapping("/manual-releases")
    public ApiResponse<IdResponse> createManualRelease(@Valid @RequestBody CreateManualReleaseRequest request) {
        return ApiResponse.ok(new IdResponse(merchantMobileService.createManualRelease(request)));
    }
}
