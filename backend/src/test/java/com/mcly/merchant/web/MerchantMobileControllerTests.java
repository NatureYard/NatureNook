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

    @Test
    @Transactional
    void shouldCreateGroomingOrder() throws Exception {
  Integer groomingOrderCountBefore = jdbcTemplate.queryForObject("select count(*) from grooming_order", Integer.class);

  mockMvc.perform(post("/api/m-app/grooming/orders")
      .contentType("application/json")
      .content("""
        {
          "storeId": 1,
          "memberId": 1,
          "petId": 1,
          "staffId": 2,
          "scheduledAt": "2026-04-06T10:30:00",
          "totalFee": 188.00,
          "remark": "首次洗护预约"
        }
        """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true))
    .andExpect(jsonPath("$.data.id").isNumber());

  Integer groomingOrderCountAfter = jdbcTemplate.queryForObject("select count(*) from grooming_order", Integer.class);
  assertThat(groomingOrderCountAfter).isEqualTo((groomingOrderCountBefore == null ? 0 : groomingOrderCountBefore) + 1);

  String status = jdbcTemplate.queryForObject(
    "select status from grooming_order order by id desc limit 1",
    String.class
  );
  String remark = jdbcTemplate.queryForObject(
    "select remark from grooming_order order by id desc limit 1",
    String.class
  );

  assertThat(status).isEqualTo("BOOKED");
  assertThat(remark).isEqualTo("首次洗护预约");
    }

    @Test
    @Transactional
    void shouldCompleteGroomingOrderAndCreateServiceRecord() throws Exception {
  jdbcTemplate.update("""
    insert into grooming_order (
        id, store_id, member_id, pet_id, staff_id,
        scheduled_at, status, total_fee, remark
    ) values (?, ?, ?, ?, ?, current_timestamp, ?, ?, ?)
    """, 1L, 1L, 1L, 1L, 2L, "BOOKED", 168.00, "待完工");

  Integer serviceRecordCountBefore = jdbcTemplate.queryForObject(
    "select count(*) from grooming_service_record",
    Integer.class
  );

  mockMvc.perform(post("/api/m-app/grooming/orders/complete")
      .contentType("application/json")
      .content("""
        {
          "orderId": 1,
          "note": "修剪指甲并完成吹干"
        }
        """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true));

  Integer serviceRecordCountAfter = jdbcTemplate.queryForObject(
    "select count(*) from grooming_service_record",
    Integer.class
  );
  String status = jdbcTemplate.queryForObject(
    "select status from grooming_order where id = ?",
    String.class,
    1L
  );
  String note = jdbcTemplate.queryForObject(
    "select note from grooming_service_record where grooming_order_id = ?",
    String.class,
    1L
  );

  assertThat(serviceRecordCountAfter).isEqualTo((serviceRecordCountBefore == null ? 0 : serviceRecordCountBefore) + 1);
  assertThat(status).isEqualTo("COMPLETED");
  assertThat(note).isEqualTo("修剪指甲并完成吹干");
    }

    @Test
    @Transactional
    void shouldCreateBoardingOrder() throws Exception {
  Integer boardingOrderCountBefore = jdbcTemplate.queryForObject("select count(*) from boarding_order", Integer.class);

  mockMvc.perform(post("/api/m-app/boarding/orders")
      .contentType("application/json")
      .content("""
        {
          "storeId": 1,
          "memberId": 2,
          "petId": 2,
          "cageNo": "C-08",
          "checkInTime": "2026-04-06T09:00:00",
          "plannedCheckOutTime": "2026-04-08T18:00:00",
          "remark": "需要单独安置"
        }
        """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true))
    .andExpect(jsonPath("$.data.id").isNumber());

  Integer boardingOrderCountAfter = jdbcTemplate.queryForObject("select count(*) from boarding_order", Integer.class);
  assertThat(boardingOrderCountAfter).isEqualTo((boardingOrderCountBefore == null ? 0 : boardingOrderCountBefore) + 1);

  String status = jdbcTemplate.queryForObject(
    "select status from boarding_order order by id desc limit 1",
    String.class
  );
  String cageNo = jdbcTemplate.queryForObject(
    "select cage_no from boarding_order order by id desc limit 1",
    String.class
  );

  assertThat(status).isEqualTo("CHECKED_IN");
  assertThat(cageNo).isEqualTo("C-08");
    }

    @Test
    @Transactional
    void shouldAddBoardingDailyRecord() throws Exception {
  jdbcTemplate.update("""
    insert into boarding_order (
        id, store_id, member_id, pet_id, cage_no,
        check_in_time, planned_check_out_time, status, remark
    ) values (?, ?, ?, ?, ?, current_timestamp, current_timestamp + 2, ?, ?)
    """, 1L, 1L, 2L, 2L, "C-08", "CHECKED_IN", "测试寄养单");

  Integer dailyRecordCountBefore = jdbcTemplate.queryForObject(
    "select count(*) from boarding_daily_record",
    Integer.class
  );

  mockMvc.perform(post("/api/m-app/boarding/daily-records")
      .contentType("application/json")
      .content("""
        {
          "boardingOrderId": 1,
          "recordDate": "2026-04-06",
          "healthNote": "进食正常，精神状态稳定",
          "exceptionNote": "无异常",
          "staffId": 2
        }
        """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.success").value(true))
    .andExpect(jsonPath("$.data.id").isNumber());

  Integer dailyRecordCountAfter = jdbcTemplate.queryForObject(
    "select count(*) from boarding_daily_record",
    Integer.class
  );
  String healthNote = jdbcTemplate.queryForObject(
    "select health_note from boarding_daily_record where boarding_order_id = ?",
    String.class,
    1L
  );
  String exceptionNote = jdbcTemplate.queryForObject(
    "select exception_note from boarding_daily_record where boarding_order_id = ?",
    String.class,
    1L
  );

  assertThat(dailyRecordCountAfter).isEqualTo((dailyRecordCountBefore == null ? 0 : dailyRecordCountBefore) + 1);
  assertThat(healthNote).isEqualTo("进食正常，精神状态稳定");
  assertThat(exceptionNote).isEqualTo("无异常");
    }
}
