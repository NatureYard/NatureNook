package com.mcly.boarding.repository;

import com.mcly.boarding.api.BoardingOrderResponse;
import com.mcly.common.repository.QuerySupport;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BoardingQueryRepository extends QuerySupport {

    public BoardingQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<BoardingOrderResponse> listOrders() {
        return query("""
                select bo.id,
                       m.name as member_name,
                       p.name as pet_name,
                       bo.cage_no,
                       to_char(bo.check_in_time, 'YYYY-MM-DD"T"HH24:MI:SS') as check_in_time,
                       to_char(bo.planned_check_out_time, 'YYYY-MM-DD"T"HH24:MI:SS') as planned_check_out_time,
                       bo.status,
                       bo.total_fee
                from boarding_order bo
                join member m on m.id = bo.member_id
                join pet_profile p on p.id = bo.pet_id
                order by bo.check_in_time desc
                """, (rs, rowNum) -> new BoardingOrderResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("pet_name"),
                rs.getString("cage_no"),
                rs.getString("check_in_time"),
                rs.getString("planned_check_out_time"),
                rs.getString("status"),
                rs.getBigDecimal("total_fee")
        ));
    }
}
