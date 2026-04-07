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
                null,
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
            Long petId,
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
                        member_id, store_id, pet_id, reservation_type, reservation_date, time_slot, status, amount, source
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, 'CUSTOMER_MINI')
                    """, new String[]{"id"});
            statement.setLong(1, memberId);
            statement.setLong(2, storeId);
            if (petId == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, petId);
            }
            statement.setString(4, reservationType);
            statement.setDate(5, Date.valueOf(reservationDate));
            statement.setString(6, timeSlot);
            statement.setString(7, status);
            statement.setBigDecimal(8, amount);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
