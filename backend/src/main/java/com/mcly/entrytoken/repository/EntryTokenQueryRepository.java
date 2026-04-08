package com.mcly.entrytoken.repository;

import com.mcly.common.repository.QuerySupport;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EntryTokenQueryRepository extends QuerySupport {

    public EntryTokenQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * 根据 token 值查找 entry_token 记录。
     */
    public Map<String, Object> findByTokenValue(String tokenValue) {
        var rows = jdbcTemplate.queryForList("""
                select id, pass_entitlement_id, member_id, store_id, status, expires_at
                from entry_token where token_value = ?
                """, tokenValue);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 判断会员当前是否在园内。
     * 查询最新一条 entry_exit_record（按 ID 降序保证顺序），如果 direction='ENTRY' 且 result='PASSED' 则在园内。
     */
    public boolean isMemberInPark(Long memberId, Long storeId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from (
                    select direction from entry_exit_record
                    where member_id = ? and store_id = ? and result = 'PASSED'
                    order by id desc limit 1
                ) sub
                where sub.direction = 'ENTRY'
                """, Integer.class, memberId, storeId);
        return count != null && count > 0;
    }
}
