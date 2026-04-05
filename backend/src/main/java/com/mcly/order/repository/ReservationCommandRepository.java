package com.mcly.order.repository;

import com.mcly.order.api.CreateReservationRequest;
import java.math.BigDecimal;
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
        return create(
                request.memberId(),
                request.storeId(),
                request.reservationType(),
                LocalDate.parse(request.reservationDate()),
                request.timeSlot(),
                request.status(),
                request.amount()
        );
    }

    public Long create(
            Long memberId,
            Long storeId,
            String reservationType,
            LocalDate reservationDate,
            String timeSlot,
            String status,
            BigDecimal amount
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into reservation (
                        member_id, store_id, reservation_type, reservation_date, time_slot, status, amount, source
                    ) values (?, ?, ?, ?, ?, ?, ?, 'CUSTOMER_MINI')
                    """, new String[]{"id"});
            statement.setLong(1, memberId);
            statement.setLong(2, storeId);
            statement.setString(3, reservationType);
            statement.setDate(4, Date.valueOf(reservationDate));
            statement.setString(5, timeSlot);
            statement.setString(6, status);
            statement.setBigDecimal(7, amount);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
