package com.mcly.merchant.repository;

import com.mcly.merchant.api.CreateManualReleaseRequest;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ManualReleaseCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public ManualReleaseCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(CreateManualReleaseRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into manual_release_record (
                        store_id, member_id, order_id, staff_id, reason, risk_flag
                    ) values (?, ?, ?, ?, ?, true)
                    """, new String[]{"id"});
            statement.setLong(1, request.storeId());
            if (request.memberId() == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, request.memberId());
            }
            if (request.orderId() == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, request.orderId());
            }
            statement.setLong(4, request.staffId());
            statement.setString(5, request.reason());
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}

