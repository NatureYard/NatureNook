package com.mcly.entrytoken.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EntryTokenCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public EntryTokenCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 创建一次性入园令牌，返回自增 ID。
     */
    public Long create(Long passEntitlementId, Long memberId, Long storeId,
                       String tokenValue, LocalDateTime expiresAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into entry_token (pass_entitlement_id, member_id, store_id, token_value, status, expires_at)
                    values (?, ?, ?, ?, 'ACTIVE', ?)
                    """, new String[]{"id"});
            ps.setLong(1, passEntitlementId);
            ps.setLong(2, memberId);
            ps.setLong(3, storeId);
            ps.setString(4, tokenValue);
            ps.setTimestamp(5, Timestamp.valueOf(expiresAt));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    /**
     * 原子消耗令牌：SELECT FOR UPDATE 后更新状态为 USED。
     * 返回受影响行数，0 表示已被消耗或不存在。
     */
    @Transactional
    public int consume(Long tokenId, Long entryExitRecordId) {
        // 先锁定行
        jdbcTemplate.queryForList("""
                select id from entry_token where id = ? for update
                """, Long.class, tokenId);

        return jdbcTemplate.update("""
                update entry_token
                set status = 'USED', consumed_at = current_timestamp, entry_exit_record_id = ?
                where id = ? and status = 'ACTIVE'
                """, entryExitRecordId, tokenId);
    }
}
