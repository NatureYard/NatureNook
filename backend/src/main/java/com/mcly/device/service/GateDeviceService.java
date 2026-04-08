package com.mcly.device.service;

import com.mcly.device.api.GateEventReportRequest;
import com.mcly.device.api.GateHeartbeatRequest;
import com.mcly.device.api.GateVerifyQrRequest;
import com.mcly.device.api.GateVerifyRequest;
import com.mcly.device.api.GateVerifyResponse;
import com.mcly.device.repository.GateDeviceCommandRepository;
import com.mcly.device.repository.GateDeviceQueryRepository;
import com.mcly.entrytoken.repository.EntryTokenCommandRepository;
import com.mcly.entrytoken.repository.EntryTokenQueryRepository;
import com.mcly.entrytoken.service.EntryTokenService;
import com.mcly.risk.repository.RiskEventCommandRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GateDeviceService {

    private static final Logger log = LoggerFactory.getLogger(GateDeviceService.class);

    private final GateDeviceQueryRepository queryRepository;
    private final GateDeviceCommandRepository commandRepository;
    private final RiskEventCommandRepository riskEventCommandRepository;
    private final EntryTokenService entryTokenService;
    private final EntryTokenQueryRepository entryTokenQueryRepository;
    private final EntryTokenCommandRepository entryTokenCommandRepository;

    public GateDeviceService(
            GateDeviceQueryRepository queryRepository,
            GateDeviceCommandRepository commandRepository,
            RiskEventCommandRepository riskEventCommandRepository,
            EntryTokenService entryTokenService,
            EntryTokenQueryRepository entryTokenQueryRepository,
            EntryTokenCommandRepository entryTokenCommandRepository
    ) {
        this.queryRepository = queryRepository;
        this.commandRepository = commandRepository;
        this.riskEventCommandRepository = riskEventCommandRepository;
        this.entryTokenService = entryTokenService;
        this.entryTokenQueryRepository = entryTokenQueryRepository;
        this.entryTokenCommandRepository = entryTokenCommandRepository;
    }

    public GateVerifyResponse verifyPass(GateVerifyRequest request) {
        Map<String, Object> device = queryRepository.findDeviceByCode(request.deviceCode());
        if (device == null) {
            return new GateVerifyResponse(request.memberId(), false, "DEVICE_NOT_FOUND", false, true);
        }

        Long storeId = toLong(device.get("store_id"));
        Long deviceId = toLong(device.get("id"));

        if (queryRepository.isMemberBlacklisted(request.memberId())) {
            commandRepository.recordEntryExit(
                    request.memberId(), storeId, deviceId, request.direction(),
                    "BLOCKED", true, "BLACKLISTED");
            riskEventCommandRepository.create(storeId, "BLACKLISTED_ENTRY_ATTEMPT", "HIGH", "MEMBER", request.memberId());
            return new GateVerifyResponse(request.memberId(), false, "BLACKLISTED", true, false);
        }

        boolean hasEntitlement = queryRepository.hasActiveEntitlement(request.memberId(), storeId);
        if (!hasEntitlement) {
            commandRepository.recordEntryExit(
                    request.memberId(), storeId, deviceId, request.direction(),
                    "BLOCKED", false, "NO_ENTITLEMENT");
            riskEventCommandRepository.create(storeId, "NO_ENTITLEMENT_ENTRY_ATTEMPT", "MEDIUM", "MEMBER", request.memberId());
            return new GateVerifyResponse(request.memberId(), false, "NO_ENTITLEMENT", false, true);
        }

        commandRepository.recordEntryExit(
                request.memberId(), storeId, deviceId, request.direction(),
                "PASSED", false, null);
        return new GateVerifyResponse(request.memberId(), true, null, false, false);
    }

    /**
     * 基于动态二维码的闸机核验。
     * 完整流程：解码二维码 → 验签 → 检查时效 → 消耗 token → 在园状态检查 → 记录通行。
     */
    @Transactional
    public GateVerifyResponse verifyQrPass(GateVerifyQrRequest request) {
        // 1. 查设备
        Map<String, Object> device = queryRepository.findDeviceByCode(request.deviceCode());
        if (device == null) {
            return new GateVerifyResponse(null, false, "DEVICE_NOT_FOUND", false, true);
        }
        Long deviceId = toLong(device.get("id"));

        // 2. 解码 + 验签
        Map<String, Object> decoded = entryTokenService.decodeQrContent(request.qrContent());
        if (decoded == null) {
            Long storeId = toLong(device.get("store_id"));
            commandRepository.recordEntryExit(null, storeId, deviceId, request.direction(),
                    "BLOCKED", true, "INVALID_QR_SIGNATURE");
            riskEventCommandRepository.create(storeId, "QR_FORGERY_ATTEMPT", "HIGH", "DEVICE", deviceId);
            return new GateVerifyResponse(null, false, "INVALID_QR_SIGNATURE", true, true);
        }

        Long passId = toLong(decoded.get("passId"));
        String tokenValue = (String) decoded.get("tokenValue");
        long epochSeconds = toLong(decoded.get("epochSeconds"));

        // 3. 查 token 记录
        Map<String, Object> tokenRecord = entryTokenQueryRepository.findByTokenValue(tokenValue);
        if (tokenRecord == null) {
            return new GateVerifyResponse(null, false, "TOKEN_NOT_FOUND", false, true);
        }

        Long tokenId = toLong(tokenRecord.get("id"));
        Long memberId = toLong(tokenRecord.get("member_id"));
        Long storeId = toLong(tokenRecord.get("store_id"));

        // 4. 检查时效
        if (!entryTokenService.isTimestampValid(epochSeconds)) {
            commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                    "BLOCKED", false, "TOKEN_EXPIRED");
            return new GateVerifyResponse(memberId, false, "TOKEN_EXPIRED", false, true);
        }

        // 4.5 检查 token 状态（已被消耗则直接拒绝，优先于在园检查）
        String tokenStatus = (String) tokenRecord.get("status");
        if (!"ACTIVE".equals(tokenStatus)) {
            riskEventCommandRepository.create(storeId, "DUPLICATE_QR_SCAN", "HIGH", "MEMBER", memberId);
            return new GateVerifyResponse(memberId, false, "TOKEN_ALREADY_USED", true, false);
        }

        // 5. 检查黑名单
        if (queryRepository.isMemberBlacklisted(memberId)) {
            commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                    "BLOCKED", true, "BLACKLISTED");
            riskEventCommandRepository.create(storeId, "BLACKLISTED_ENTRY_ATTEMPT", "HIGH", "MEMBER", memberId);
            return new GateVerifyResponse(memberId, false, "BLACKLISTED", true, false);
        }

        // 6. 检查通行资格
        boolean hasEntitlement = queryRepository.hasActiveEntitlement(memberId, storeId);
        if (!hasEntitlement) {
            commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                    "BLOCKED", false, "NO_ENTITLEMENT");
            riskEventCommandRepository.create(storeId, "NO_ENTITLEMENT_ENTRY_ATTEMPT", "MEDIUM", "MEMBER", memberId);
            return new GateVerifyResponse(memberId, false, "NO_ENTITLEMENT", false, true);
        }

        // 7. 检查在园状态（仅入园方向）
        if ("ENTRY".equals(request.direction()) && entryTokenQueryRepository.isMemberInPark(memberId, storeId)) {
            commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                    "BLOCKED", false, "ALREADY_IN_PARK");
            riskEventCommandRepository.create(storeId, "DUPLICATE_ENTRY_ATTEMPT", "HIGH", "MEMBER", memberId);
            return new GateVerifyResponse(memberId, false, "ALREADY_IN_PARK", true, false);
        }

        // 出园方向：检查不在园
        if ("EXIT".equals(request.direction()) && !entryTokenQueryRepository.isMemberInPark(memberId, storeId)) {
            commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                    "BLOCKED", false, "NOT_IN_PARK");
            return new GateVerifyResponse(memberId, false, "NOT_IN_PARK", false, true);
        }

        // 8. 原子消耗 token
        // 先记录通行，拿到 entry_exit_record ID
        commandRepository.recordEntryExit(memberId, storeId, deviceId, request.direction(),
                "PASSED", false, null);

        // 查最新记录 ID
        Long entryExitRecordId = queryRepository.findLatestEntryExitId(memberId, storeId);
        int consumed = entryTokenCommandRepository.consume(tokenId, entryExitRecordId);
        if (consumed == 0) {
            // token 已被消耗 — 重复扫描
            riskEventCommandRepository.create(storeId, "DUPLICATE_QR_SCAN", "HIGH", "MEMBER", memberId);
            return new GateVerifyResponse(memberId, false, "TOKEN_ALREADY_USED", true, false);
        }

        log.info("二维码核验通过: memberId={}, direction={}, deviceCode={}", memberId, request.direction(), request.deviceCode());

        return new GateVerifyResponse(memberId, true, null, false, false);
    }

    public void reportEvent(GateEventReportRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), "ONLINE");
        commandRepository.logGateEvent(request.deviceCode(), request.eventType(), request.detail());
    }

    public void heartbeat(GateHeartbeatRequest request) {
        commandRepository.updateHeartbeat(request.deviceCode(), request.status());
    }

    private static Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.valueOf(val.toString());
    }
}
