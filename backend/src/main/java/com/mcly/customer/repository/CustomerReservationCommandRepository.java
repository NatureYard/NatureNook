package com.mcly.customer.repository;

import com.mcly.customer.api.CreateCustomerReservationRequest;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerReservationCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerReservationCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(CreateCustomerReservationRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into reservation (
                        member_id, store_id, reservation_type, reservation_date,
                        time_slot, status, amount, source
                    ) values (?, ?, ?, ?, ?, 'PENDING', ?, 'CUSTOMER_MINI')
                    """, new String[]{"id"});
            statement.setLong(1, request.memberId());
            statement.setLong(2, request.storeId());
            statement.setString(3, request.reservationType());
            statement.setDate(4, Date.valueOf(request.reservationDate()));
            statement.setString(5, request.timeSlot());
            statement.setBigDecimal(6, request.amount() != null ? request.amount() : BigDecimal.ZERO);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
