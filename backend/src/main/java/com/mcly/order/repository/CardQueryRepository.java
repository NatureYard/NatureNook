package com.mcly.order.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.order.api.CardSummaryResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CardQueryRepository extends QuerySupport {

    public CardQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<CardSummaryResponse> listCards() {
        return query("""
                select mc.id,
                       m.name as member_name,
                       mc.card_type,
                       mc.status,
                       to_char(mc.valid_to, 'YYYY-MM-DD') as valid_to
                from membership_card mc
                join member m on m.id = mc.member_id
                order by mc.id
                """, (rs, rowNum) -> new CardSummaryResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("card_type"),
                rs.getString("status"),
                rs.getString("valid_to")
        ));
    }
}

