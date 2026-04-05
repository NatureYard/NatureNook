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
    void shouldCreateReservationOrderAndPassEntitlement() throws Exception {
        Integer reservationCountBefore = jdbcTemplate.queryForObject("select count(*) from reservation", Integer.class);
        Integer orderCountBefore = jdbcTemplate.queryForObject("select count(*) from customer_order", Integer.class);
        Integer entitlementCountBefore = jdbcTemplate.queryForObject("select count(*) from pass_entitlement", Integer.class);

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
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.orderNo").value(startsWith("ORD20260406")));

        Integer reservationCountAfter = jdbcTemplate.queryForObject("select count(*) from reservation", Integer.class);
        Integer orderCountAfter = jdbcTemplate.queryForObject("select count(*) from customer_order", Integer.class);
        Integer entitlementCountAfter = jdbcTemplate.queryForObject("select count(*) from pass_entitlement", Integer.class);

        assertThat(reservationCountAfter).isEqualTo((reservationCountBefore == null ? 0 : reservationCountBefore) + 1);
        assertThat(orderCountAfter).isEqualTo((orderCountBefore == null ? 0 : orderCountBefore) + 1);
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
}
