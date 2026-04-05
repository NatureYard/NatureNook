package com.mcly.gate.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.gate.api.GateEventResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GateQueryRepository extends QuerySupport {

    public GateQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<GateEventResponse> listLatestEvents() {
        return query("""
                select eer.id,
                       coalesce(m.name, '未识别会员') as member_name,
                       eer.direction,
                       eer.result,
                       to_char(eer.occurred_at, 'YYYY-MM-DD"T"HH24:MI:SS') as occurred_at
                from entry_exit_record eer
                left join member m on m.id = eer.member_id
                order by eer.occurred_at desc
                limit 10
                """, (rs, rowNum) -> new GateEventResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("direction"),
                rs.getString("result"),
                rs.getString("occurred_at")
        ));
    }
}

