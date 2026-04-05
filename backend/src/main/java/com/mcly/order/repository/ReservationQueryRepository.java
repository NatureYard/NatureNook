package com.mcly.order.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.order.api.ReservationSummaryResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationQueryRepository extends QuerySupport {

    public ReservationQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<ReservationSummaryResponse> listReservations() {
        return query("""
                select r.id,
                       m.name as member_name,
                       s.name as store_name,
                       r.reservation_type,
                       to_char(r.reservation_date, 'YYYY-MM-DD') as reservation_date,
                       r.time_slot,
                       r.status
                from reservation r
                join member m on m.id = r.member_id
                join store s on s.id = r.store_id
                order by r.reservation_date desc, r.id desc
                """, (rs, rowNum) -> new ReservationSummaryResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("store_name"),
                rs.getString("reservation_type"),
                rs.getString("reservation_date"),
                rs.getString("time_slot"),
                rs.getString("status")
        ));
    }
}

