package com.mcly.customer.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerMiniControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnCurrentCustomerContext() throws Exception {
        mockMvc.perform(get("/api/c-app/context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.memberName").value("张三"))
                .andExpect(jsonPath("$.data.storeName").value("上海萌宠乐园旗舰店"))
                .andExpect(jsonPath("$.data.pets[0].name").value("奶球"));
    }

    @Test
    void shouldFilterOrdersByCurrentCustomer() throws Exception {
        mockMvc.perform(get("/api/c-app/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORD202604050001"))
                .andExpect(jsonPath("$.data[0].type").value("单次门票"))
                .andExpect(jsonPath("$.data[0].status").value("PAID"))
                .andExpect(jsonPath("$.data[0].amount").value("68.00"))
                .andExpect(jsonPath("$.data[0].storeName").value("上海萌宠乐园旗舰店"))
                .andExpect(jsonPath("$.data[0].reservationDate").exists())
                .andExpect(jsonPath("$.data[0].timeSlot").value("09:00-12:00"));
    }

    @Test
    void shouldReturnActivePasses() throws Exception {
        mockMvc.perform(get("/api/c-app/passes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("单次门票入园资格"))
          .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
          .andExpect(jsonPath("$.data[0].storeName").value("上海萌宠乐园旗舰店"))
          .andExpect(jsonPath("$.data[0].validFrom").exists())
          .andExpect(jsonPath("$.data[0].validTo").exists())
                .andExpect(jsonPath("$.data[0].reentryPolicy").value("SAME_DAY_UNLIMITED"));
    }

    @Test
    @Transactional
    void shouldCreateReservationWithPendingPayStatus() throws Exception {
        Integer reservationCountBefore = jdbcTemplate.queryForObject("select count(*) from reservation", Integer.class);
        Integer orderCountBefore = jdbcTemplate.queryForObject("select count(*) from customer_order", Integer.class);

        mockMvc.perform(post("/api/c-app/reservations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "ticketCode": "DAY_TICKET",
                                  "reservationDate": "2026-04-06",
                                  "timeSlot": "09:00-12:00",
                                  "petId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAY"))
                .andExpect(jsonPath("$.data.orderNo").value(startsWith("ORD20260406")));

        Integer reservationCountAfter = jdbcTemplate.queryForObject("select count(*) from reservation", Integer.class);
        Integer orderCountAfter = jdbcTemplate.queryForObject("select count(*) from customer_order", Integer.class);

        assertThat(reservationCountAfter).isEqualTo((reservationCountBefore == null ? 0 : reservationCountBefore) + 1);
        assertThat(orderCountAfter).isEqualTo((orderCountBefore == null ? 0 : orderCountBefore) + 1);

        // 验证订单状态是 PENDING_PAY
        String orderStatus = jdbcTemplate.queryForObject(
                "select status from customer_order where id = (select max(id) from customer_order)",
                String.class
        );
        assertThat(orderStatus).isEqualTo("PENDING_PAY");
    }

    @Test
    @Transactional
    void shouldCompleteFullPaymentFlow() throws Exception {
        // Step 1: 创建预约（订单为 PENDING_PAY）
        var createResult = mockMvc.perform(post("/api/c-app/reservations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "ticketCode": "DAY_TICKET",
                                  "reservationDate": "2026-04-06",
                                  "timeSlot": "09:00-12:00",
                                  "petId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_PAY"))
                .andReturn();

        // 从响应中提取 orderNo
        String responseBody = createResult.getResponse().getContentAsString();
        String orderNo = com.fasterxml.jackson.databind.ObjectMapper
                .class.getDeclaredConstructor().newInstance()
                .readTree(responseBody).get("data").get("orderNo").asText();

        // Step 2: 预支付
        mockMvc.perform(post("/api/c-app/prepay")
                        .contentType("application/json")
                        .content("{\"orderNo\": \"" + orderNo + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timeStamp").exists())
                .andExpect(jsonPath("$.data.nonceStr").exists())
                .andExpect(jsonPath("$.data.paySign").exists())
                .andExpect(jsonPath("$.data.orderNo").value(orderNo));

        // Step 3: 确认支付
        Integer entitlementCountBefore = jdbcTemplate.queryForObject("select count(*) from pass_entitlement", Integer.class);

        mockMvc.perform(post("/api/c-app/payment/confirm")
                        .contentType("application/json")
                        .content("{\"orderNo\": \"" + orderNo + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证订单变为 PAID
        String finalStatus = jdbcTemplate.queryForObject(
                "select status from customer_order where order_no = ?",
                String.class, orderNo
        );
        assertThat(finalStatus).isEqualTo("PAID");

        // 验证通行资格已生成
        Integer entitlementCountAfter = jdbcTemplate.queryForObject("select count(*) from pass_entitlement", Integer.class);
        assertThat(entitlementCountAfter).isEqualTo((entitlementCountBefore == null ? 0 : entitlementCountBefore) + 1);
    }

    @Test
    void shouldRejectReservationForOtherMembersPet() throws Exception {
        mockMvc.perform(post("/api/c-app/reservations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "ticketCode": "DAY_TICKET",
                                  "reservationDate": "2026-04-06",
                                  "timeSlot": "09:00-12:00",
                                  "petId": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("宠物不存在或不属于当前会员"));
    }

    @Test
    @Transactional
    void shouldLoginWithWxCode() throws Exception {
        mockMvc.perform(post("/api/c-app/login")
                        .contentType("application/json")
                        .content("{\"code\": \"test_code_001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.memberName").exists());
    }
}
