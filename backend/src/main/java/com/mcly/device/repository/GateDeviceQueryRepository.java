package com.mcly.device.repository;

import com.mcly.common.repository.QuerySupport;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GateDeviceQueryRepository extends QuerySupport {

    public GateDeviceQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public Map<String, Object> findDeviceByCode(String deviceCode) {
        var rows = jdbcTemplate.queryForList("""
                select id, store_id, status from gate_device where code = ?
                """, deviceCode);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean hasActiveEntitlement(Long memberId, Long storeId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from pass_entitlement
                where member_id = ? and store_id = ? and status = 'ACTIVE'
                  and valid_from <= current_timestamp and valid_to >= current_timestamp
                """, Integer.class, memberId, storeId);
        return count != null && count > 0;
    }

    public boolean isMemberBlacklisted(Long memberId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from member where id = ? and risk_tag = 'BLACKLIST'
                """, Integer.class, memberId);
        return count != null && count > 0;
    }

    /**
     * 查询会员最新一条 entry_exit_record 的 ID。
     */
    public Long findLatestEntryExitId(Long memberId, Long storeId) {
        var rows = jdbcTemplate.queryForList("""
                select id from entry_exit_record
                where member_id = ? and store_id = ?
                order by occurred_at desc limit 1
                """, memberId, storeId);
        return rows.isEmpty() ? null : ((Number) rows.get(0).get("id")).longValue();
    }
}
