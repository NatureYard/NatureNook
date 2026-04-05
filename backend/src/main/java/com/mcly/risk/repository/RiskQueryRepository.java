package com.mcly.risk.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.risk.api.RiskEventResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RiskQueryRepository extends QuerySupport {

    public RiskQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<RiskEventResponse> listOpenEvents() {
        return query("""
                select re.id,
                       s.name as store_name,
                       re.event_type,
                       re.event_level,
                       re.subject_type,
                       re.subject_id,
                       re.content::text as content,
                       re.status,
                       to_char(re.created_at, 'YYYY-MM-DD"T"HH24:MI:SS') as created_at
                from risk_event re
                left join store s on s.id = re.store_id
                order by re.created_at desc
                limit 50
                """, (rs, rowNum) -> new RiskEventResponse(
                rs.getLong("id"),
                rs.getString("store_name"),
                rs.getString("event_type"),
                rs.getString("event_level"),
                rs.getString("subject_type"),
                rs.getLong("subject_id"),
                rs.getString("content"),
                rs.getString("status"),
                rs.getString("created_at")
        ));
    }
}
