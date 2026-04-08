package com.mcly.device.web;

import com.mcly.common.config.GateProperties;
import com.mcly.entrytoken.service.EntryTokenService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GateDeviceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GateProperties gateProperties;

    @Test
    @Transactional
    void shouldAllowEntryWithValidQrToken() throws Exception {
        // 生成有效 token
        String qrContent = generateValidQrContent(1L);

        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1));

        // 验证 entry_exit_record 已记录
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from entry_exit_record where member_id = 1 and result = 'PASSED' and direction = 'ENTRY'",
                Integer.class);
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    void shouldRejectExpiredQrToken() throws Exception {
        // 生成一个已过期的 token（epochSeconds 设为过去）
        long pastEpoch = Instant.now().getEpochSecond() - 120;
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        String payload = buildPayload(1L, tokenValue, pastEpoch);
        String sig = sign(payload, gateProperties.hmacSecret());
        String qrContent = payload + "." + sig;

        // 先在 DB 中插入 token
        jdbcTemplate.update("""
                insert into entry_token (pass_entitlement_id, member_id, store_id, token_value, status, expires_at)
                values (1, 1, 1, ?, 'ACTIVE', current_timestamp - interval '2' minute)
                """, tokenValue);

        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reasonCode").value("TOKEN_EXPIRED"));
    }

    @Test
    @Transactional
    void shouldRejectAlreadyConsumedToken() throws Exception {
        // 第一次扫描
        String qrContent = generateValidQrContent(1L);

        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        // 第二次扫描同一 token
        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reasonCode").value("TOKEN_ALREADY_USED"));
    }

    @Test
    @Transactional
    void shouldRejectInvalidQrSignature() throws Exception {
        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"invalid.content.fakesig","direction":"ENTRY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reasonCode").value("INVALID_QR_SIGNATURE"));

        // 验证风控事件已创建
        Integer riskCount = jdbcTemplate.queryForObject(
                "select count(*) from risk_event where event_type = 'QR_FORGERY_ATTEMPT'",
                Integer.class);
        assertThat(riskCount).isGreaterThan(0);
    }

    @Test
    @Transactional
    void shouldRejectEntryWhenAlreadyInPark() throws Exception {
        // 先模拟已入园状态
        String qrContent1 = generateValidQrContent(1L);

        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        // 生成新 token，尝试再次入园
        String qrContent2 = generateValidQrContent(1L);

        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reasonCode").value("ALREADY_IN_PARK"));
    }

    @Test
    @Transactional
    void shouldAllowReentryAfterExit() throws Exception {
        // 入园
        String qrContent1 = generateValidQrContent(1L);
        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        // 出园（使用旧的 verify-pass 端点模拟出园）
        mockMvc.perform(post("/api/device/gate/verify-pass")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","memberId":1,"direction":"EXIT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        // 再生成新 token 入园
        String qrContent2 = generateValidQrContent(1L);
        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));
    }

    @Test
    @Transactional
    void shouldAllowExitWhenInPark() throws Exception {
        // 先入园
        String qrContent = generateValidQrContent(1L);
        mockMvc.perform(post("/api/device/gate/verify-qr")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","qrContent":"%s","direction":"ENTRY"}
                                """.formatted(qrContent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        // 出园
        mockMvc.perform(post("/api/device/gate/verify-pass")
                        .contentType("application/json")
                        .content("""
                                {"deviceCode":"GATE-SH-001","memberId":1,"direction":"EXIT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));
    }

    // ---- 辅助方法 ----

    private String generateValidQrContent(Long passEntitlementId) {
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        long epochSeconds = Instant.now().getEpochSecond();
        String payload = buildPayload(passEntitlementId, tokenValue, epochSeconds);
        String sig = sign(payload, gateProperties.hmacSecret());

        // 在 DB 中插入 token
        jdbcTemplate.update("""
                insert into entry_token (pass_entitlement_id, member_id, store_id, token_value, status, expires_at)
                values (?, 1, 1, ?, 'ACTIVE', current_timestamp + interval '1' minute)
                """, passEntitlementId, tokenValue);

        return payload + "." + sig;
    }

    private String buildPayload(Long passId, String tokenValue, long epochSeconds) {
        String json = String.format("{\"p\":%d,\"t\":\"%s\",\"ts\":%d}", passId, tokenValue, epochSeconds);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
