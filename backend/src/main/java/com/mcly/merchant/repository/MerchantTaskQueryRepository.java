package com.mcly.merchant.repository;

import com.mcly.merchant.api.MerchantTaskResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantTaskQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public MerchantTaskQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MerchantTaskResponse> taskBoard() {
        Long checkin = jdbcTemplate.queryForObject("select count(*) from reservation where reservation_date = current_date and status in ('PAID', 'BOOKED')", Long.class);
        Long materials = jdbcTemplate.queryForObject("""
                select count(*)
                from material_stock ms
                join material_item mi on mi.id = ms.material_item_id
                where ms.quantity < mi.safety_stock
                """, Long.class);
        Long risk = jdbcTemplate.queryForObject("select count(*) from risk_event where status = 'OPEN'", Long.class);
        Long boarding = 5L;
        return List.of(
                new MerchantTaskResponse("CHECKIN", "待核销到店", defaultLong(checkin).intValue()),
                new MerchantTaskResponse("BOARDING", "待记录寄养", boarding.intValue()),
                new MerchantTaskResponse("MATERIAL", "待补货物资", defaultLong(materials).intValue()),
                new MerchantTaskResponse("RISK", "待复核异常", defaultLong(risk).intValue())
        );
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}

