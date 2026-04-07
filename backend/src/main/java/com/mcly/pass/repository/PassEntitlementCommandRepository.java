package com.mcly.pass.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PassEntitlementCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public PassEntitlementCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createDayAccessEntitlement(
            Long memberId,
            Long storeId,
            String sourceType,
            Long sourceId,
            LocalDate validDate,
            String reentryPolicy
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime validFrom = validDate.atStartOfDay();
        LocalDateTime validTo = validDate.plusDays(1).atStartOfDay().minusSeconds(1);
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into pass_entitlement (
                        member_id, store_id, source_type, source_id, status, valid_from, valid_to, reentry_policy
                    ) values (?, ?, ?, ?, 'ACTIVE', ?, ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, memberId);
            statement.setLong(2, storeId);
            statement.setString(3, sourceType);
            statement.setLong(4, sourceId);
            statement.setTimestamp(5, Timestamp.valueOf(validFrom));
            statement.setTimestamp(6, Timestamp.valueOf(validTo));
            statement.setString(7, reentryPolicy);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
