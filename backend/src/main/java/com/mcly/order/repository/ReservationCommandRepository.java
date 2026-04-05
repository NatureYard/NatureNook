package com.mcly.order.repository;

import com.mcly.order.api.CreateReservationRequest;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(CreateReservationRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into reservation (
                        member_id, store_id, reservation_type, reservation_date, time_slot, status, amount, source
                    ) values (?, ?, ?, ?, ?, ?, ?, 'CUSTOMER_MINI')
                    """, new String[]{"id"});
            statement.setLong(1, request.memberId());
            statement.setLong(2, request.storeId());
            statement.setString(3, request.reservationType());
            statement.setDate(4, Date.valueOf(LocalDate.parse(request.reservationDate())));
            statement.setString(5, request.timeSlot());
            statement.setString(6, request.status());
            statement.setBigDecimal(7, request.amount());
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
