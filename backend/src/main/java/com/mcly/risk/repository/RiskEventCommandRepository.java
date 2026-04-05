package com.mcly.risk.repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class RiskEventCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public RiskEventCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(
            Long storeId,
            String eventType,
            String eventLevel,
            String subjectType,
            Long subjectId
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into risk_event (
                        store_id, event_type, event_level, subject_type, subject_id, content, status
                    ) values (?, ?, ?, ?, ?, ?, 'OPEN')
                    """, new String[]{"id"});
            if (storeId == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, storeId);
            }
            statement.setString(2, eventType);
            statement.setString(3, eventLevel);
            statement.setString(4, subjectType);
            if (subjectId == null) {
                statement.setNull(5, Types.BIGINT);
            } else {
                statement.setLong(5, subjectId);
            }
            statement.setObject(6, null);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
