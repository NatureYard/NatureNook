package com.mcly.merchant.service;

import com.mcly.device.api.GateVerifyRequest;
import com.mcly.device.api.GateVerifyResponse;
import com.mcly.device.service.GateDeviceService;
import com.mcly.merchant.api.CreateManualReleaseRequest;
import com.mcly.merchant.api.MerchantCheckinOrderResponse;
import com.mcly.merchant.api.MerchantCheckinVerifyRequest;
import com.mcly.merchant.api.MerchantCheckinVerifyResponse;
import com.mcly.merchant.api.MerchantTaskResponse;
import com.mcly.merchant.repository.MerchantCheckinQueryRepository;
import com.mcly.merchant.repository.ManualReleaseCommandRepository;
import com.mcly.merchant.repository.MerchantTaskQueryRepository;
import com.mcly.risk.repository.RiskEventCommandRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MerchantMobileService {

    private final MerchantTaskQueryRepository merchantTaskQueryRepository;
    private final ManualReleaseCommandRepository manualReleaseCommandRepository;
    private final MerchantCheckinQueryRepository merchantCheckinQueryRepository;
    private final GateDeviceService gateDeviceService;
    private final RiskEventCommandRepository riskEventCommandRepository;

    public MerchantMobileService(
            MerchantTaskQueryRepository merchantTaskQueryRepository,
            ManualReleaseCommandRepository manualReleaseCommandRepository,
            MerchantCheckinQueryRepository merchantCheckinQueryRepository,
            GateDeviceService gateDeviceService,
            RiskEventCommandRepository riskEventCommandRepository
    ) {
        this.merchantTaskQueryRepository = merchantTaskQueryRepository;
        this.manualReleaseCommandRepository = manualReleaseCommandRepository;
        this.merchantCheckinQueryRepository = merchantCheckinQueryRepository;
        this.gateDeviceService = gateDeviceService;
        this.riskEventCommandRepository = riskEventCommandRepository;
    }

    public List<MerchantTaskResponse> taskBoard() {
        return merchantTaskQueryRepository.taskBoard();
    }

    public Long createManualRelease(CreateManualReleaseRequest request) {
        Long id = manualReleaseCommandRepository.create(request);
        riskEventCommandRepository.create(
                request.storeId(),
                "MANUAL_RELEASE_RECORDED",
                request.orderId() == null ? "HIGH" : "MEDIUM",
                "STAFF",
                request.staffId()
        );
        return id;
    }

    public List<MerchantCheckinOrderResponse> listCheckinOrders(Long storeId) {
        return merchantCheckinQueryRepository.listTodayOrders(storeId == null ? 1L : storeId);
    }

    public MerchantCheckinVerifyResponse verifyCheckin(MerchantCheckinVerifyRequest request) {
        Map<String, Object> order = merchantCheckinQueryRepository.findOrderById(request.orderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        Long orderId = ((Number) order.get("order_id")).longValue();
        String orderNo = String.valueOf(order.get("order_no"));
        Long memberId = ((Number) order.get("member_id")).longValue();
        Long storeId = ((Number) order.get("store_id")).longValue();
        String orderStatus = String.valueOf(order.get("order_status"));
        String memberName = String.valueOf(order.get("member_name"));

        if (!List.of("PAID", "BOOKED").contains(orderStatus)) {
            riskEventCommandRepository.create(storeId, "INVALID_ORDER_STATUS_CHECKIN", "MEDIUM", "ORDER", orderId);
            return new MerchantCheckinVerifyResponse(
                    orderId,
                    orderNo,
                    memberName,
                    false,
                    "ORDER_STATUS_INVALID",
                    true,
                    true
            );
        }

        GateVerifyResponse response = gateDeviceService.verifyPass(
                new GateVerifyRequest(request.deviceCode(), memberId, request.direction())
        );
        return new MerchantCheckinVerifyResponse(
                orderId,
                orderNo,
                memberName,
                response.allowed(),
                response.reasonCode(),
                response.riskFlagged(),
                response.needManualReview()
        );
    }
}
