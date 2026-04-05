package com.mcly.order.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerOrderCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerOrderCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(
            Long memberId,
            Long storeId,
            Long reservationId,
            String orderNo,
            String orderType,
            String status,
            BigDecimal amount
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into customer_order (
                        member_id, store_id, reservation_id, order_no, order_type, status, payable_amount, paid_amount
                    ) values (?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, memberId);
            statement.setLong(2, storeId);
            statement.setLong(3, reservationId);
            statement.setString(4, orderNo);
            statement.setString(5, orderType);
            statement.setString(6, status);
            statement.setBigDecimal(7, amount);
            statement.setBigDecimal(8, amount);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
