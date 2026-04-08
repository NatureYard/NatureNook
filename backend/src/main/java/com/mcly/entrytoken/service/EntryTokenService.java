package com.mcly.entrytoken.service;

import com.mcly.common.config.GateProperties;
import com.mcly.common.repository.QuerySupport;
import com.mcly.entrytoken.api.QrCodeGenerateResponse;
import com.mcly.entrytoken.repository.EntryTokenCommandRepository;
import com.mcly.entrytoken.repository.EntryTokenQueryRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryTokenService extends QuerySupport {

    private static final Logger log = LoggerFactory.getLogger(EntryTokenService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final EntryTokenQueryRepository entryTokenQueryRepository;
    private final EntryTokenCommandRepository entryTokenCommandRepository;
    private final GateProperties gateProperties;

    public EntryTokenService(JdbcTemplate jdbcTemplate,
                             EntryTokenQueryRepository entryTokenQueryRepository,
                             EntryTokenCommandRepository entryTokenCommandRepository,
                             GateProperties gateProperties) {
        super(jdbcTemplate);
        this.entryTokenQueryRepository = entryTokenQueryRepository;
        this.entryTokenCommandRepository = entryTokenCommandRepository;
        this.gateProperties = gateProperties;
    }

    /**
     * 为指定的通行资格生成一次性动态二维码。
     * 返回签名的 Base64URL 字符串，闸机扫码后传给 /verify-qr 端点。
     */
    @Transactional
    public QrCodeGenerateResponse generateQrCode(Long passEntitlementId, Long currentMemberId) {
        // 查通行资格
        var rows = jdbcTemplate.queryForList("""
                select pe.id, pe.member_id, pe.store_id, pe.source_type, pe.status,
                       pe.valid_from, pe.valid_to, s.name as store_name
                from pass_entitlement pe
                join store s on s.id = pe.store_id
                where pe.id = ?
                """, passEntitlementId);
        Map<String, Object> entitlement = rows.isEmpty() ? null : rows.get(0);

        if (entitlement == null) {
            throw new IllegalArgumentException("通行资格不存在");
        }

        Long memberId = toLong(entitlement.get("member_id"));
        Long storeId = toLong(entitlement.get("store_id"));

        // 验证归属
        if (!memberId.equals(currentMemberId)) {
            throw new IllegalArgumentException("该通行资格不属于当前会员");
        }

        // 验证状态
        if (!"ACTIVE".equals(entitlement.get("status"))) {
            throw new IllegalArgumentException("通行资格已失效");
        }

        // 检查是否已在园内
        if (entryTokenQueryRepository.isMemberInPark(memberId, storeId)) {
            throw new IllegalArgumentException("您当前已在园内，请先出园后再生成新的入园凭证");
        }

        // 生成一次性 token
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(gateProperties.tokenTtlSeconds());

        entryTokenCommandRepository.create(passEntitlementId, memberId, storeId, tokenValue, expiresAt);

        // 构建签名二维码内容
        long epochSeconds = Instant.now().getEpochSecond();
        String payload = buildPayload(passEntitlementId, tokenValue, epochSeconds);
        String signature = sign(payload, gateProperties.hmacSecret());
        String qrContent = payload + "." + signature;

        String passName = toEntitlementName((String) entitlement.get("source_type"));
        String storeName = (String) entitlement.get("store_name");

        log.info("生成入园二维码: passEntitlementId={}, memberId={}, expiresAt={}",
                passEntitlementId, memberId, expiresAt);

        return new QrCodeGenerateResponse(
                qrContent,
                expiresAt.format(FMT),
                passEntitlementId,
                passName,
                storeName
        );
    }

    /**
     * 解码二维码内容，提取 payload 中的字段。
     * 返回 Map 包含 passId, tokenValue, epochSeconds, signature。
     */
    public Map<String, Object> decodeQrContent(String qrContent) {
        int lastDot = qrContent.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        String payload = qrContent.substring(0, lastDot);
        String signature = qrContent.substring(lastDot + 1);

        // 验签
        String expectedSig = sign(payload, gateProperties.hmacSecret());
        if (!expectedSig.equals(signature)) {
            return null;
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            // 简易 JSON 解析（不依赖 Jackson）
            Long passId = extractJsonLong(json, "p");
            String tokenValue = extractJsonString(json, "t");
            Long epochSeconds = extractJsonLong(json, "ts");

            if (passId == null || tokenValue == null || epochSeconds == null) {
                return null;
            }

            return Map.of(
                    "passId", passId,
                    "tokenValue", tokenValue,
                    "epochSeconds", epochSeconds,
                    "signature", signature
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查时间戳是否在有效期内。
     */
    public boolean isTimestampValid(long epochSeconds) {
        long now = Instant.now().getEpochSecond();
        return epochSeconds > 0 && Math.abs(now - epochSeconds) <= gateProperties.tokenTtlSeconds();
    }

    // ---- 内部方法 ----

    private String buildPayload(Long passId, String tokenValue, long epochSeconds) {
        String json = String.format("{\"p\":%d,\"t\":\"%s\",\"ts\":%d}", passId, tokenValue, epochSeconds);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC 签名失败", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.valueOf(val.toString());
    }

    private static Long extractJsonLong(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) return null;
        start += pattern.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.valueOf(json.substring(start, end));
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String toEntitlementName(String sourceType) {
        return switch (sourceType) {
            case "DAY_TICKET" -> "单次门票入园资格";
            case "GROOMING_PACKAGE" -> "洗护到店资格";
            case "BOARDING_DAY" -> "寄养到店资格";
            case "YEAR_CARD" -> "年卡入园资格";
            case "MONTH_CARD" -> "月卡入园资格";
            case "SEASON_CARD" -> "季卡入园资格";
            default -> sourceType;
        };
    }
}
