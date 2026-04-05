package com.mcly.merchant.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MerchantMobileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldListTodayCheckinOrders() throws Exception {
        mockMvc.perform(get("/api/m-app/checkin/orders").param("storeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORD202604050002"))
              .andExpect(jsonPath("$.data[0].memberName").value("李四"))
              .andExpect(jsonPath("$.data[0].petName").value("布丁"))
              .andExpect(jsonPath("$.data[0].orderType").value("GROOMING"))
              .andExpect(jsonPath("$.data[0].orderStatus").value("PAID"))
              .andExpect(jsonPath("$.data[0].storeName").value("上海萌宠乐园旗舰店"))
              .andExpect(jsonPath("$.data[0].reservationDate").exists())
              .andExpect(jsonPath("$.data[0].timeSlot").value("13:00-15:00"))
              .andExpect(jsonPath("$.data[0].faceBound").value(true))
              .andExpect(jsonPath("$.data[0].activeEntitlement").value(false))
              .andExpect(jsonPath("$.data[0].entitlementName").value(""))
              .andExpect(jsonPath("$.data[0].entitlementValidTo").value(""))
              .andExpect(jsonPath("$.data[1].memberName").value("张三"))
                .andExpect(jsonPath("$.data[1].activeEntitlement").value(true));
    }

    @Test
    @Transactional
    void shouldVerifyCheckinAgainstGateDevice() throws Exception {
        Integer entryExitCountBefore = jdbcTemplate.queryForObject("select count(*) from entry_exit_record", Integer.class);

        mockMvc.perform(post("/api/m-app/checkin/verify")
                        .contentType("application/json")
                        .content("""
                                {
                                  "orderId": 1,
                                  "deviceCode": "GATE-SH-001",
                                  "direction": "ENTRY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORD202604050001"))
                .andExpect(jsonPath("$.data.memberName").value("张三"))
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.reasonCode").doesNotExist())
                .andExpect(jsonPath("$.data.riskFlagged").value(false))
                .andExpect(jsonPath("$.data.needManualReview").value(false));

        Integer entryExitCountAfter = jdbcTemplate.queryForObject("select count(*) from entry_exit_record", Integer.class);
        assertThat(entryExitCountAfter).isEqualTo((entryExitCountBefore == null ? 0 : entryExitCountBefore) + 1);
    }

    @Test
    @Transactional
    void shouldCreateRiskEventWhenManualReleaseRecorded() throws Exception {
        Integer manualReleaseCountBefore = jdbcTemplate.queryForObject("select count(*) from manual_release_record", Integer.class);
        Integer riskCountBefore = jdbcTemplate.queryForObject("select count(*) from risk_event", Integer.class);

        mockMvc.perform(post("/api/m-app/manual-releases")
                        .contentType("application/json")
                        .content("""
                                {
                                  "storeId": 1,
                                  "memberId": 1,
                                  "orderId": 1,
                                  "staffId": 2,
                                  "reason": "闸机识别失败，现场复核证件后放行"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber());

        Integer manualReleaseCountAfter = jdbcTemplate.queryForObject("select count(*) from manual_release_record", Integer.class);
        Integer riskCountAfter = jdbcTemplate.queryForObject("select count(*) from risk_event", Integer.class);

        assertThat(manualReleaseCountAfter).isEqualTo((manualReleaseCountBefore == null ? 0 : manualReleaseCountBefore) + 1);
        assertThat(riskCountAfter).isEqualTo((riskCountBefore == null ? 0 : riskCountBefore) + 1);
    }
}
