package com.mcly.grooming.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.grooming.api.GroomingOrderResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GroomingQueryRepository extends QuerySupport {

    public GroomingQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<GroomingOrderResponse> listOrders() {
        return query("""
                select go.id,
                       m.name as member_name,
                       p.name as pet_name,
                       s.name as staff_name,
                       to_char(go.scheduled_at, 'YYYY-MM-DD"T"HH24:MI:SS') as scheduled_at,
                       go.status,
                       go.total_fee
                from grooming_order go
                join member m on m.id = go.member_id
                join pet_profile p on p.id = go.pet_id
                left join staff s on s.id = go.staff_id
                order by go.created_at desc
                """, (rs, rowNum) -> new GroomingOrderResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("pet_name"),
                rs.getString("staff_name"),
                rs.getString("scheduled_at"),
                rs.getString("status"),
                rs.getBigDecimal("total_fee")
        ));
    }
}
