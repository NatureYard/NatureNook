package com.mcly.grooming.repository;

import com.mcly.grooming.api.CompleteGroomingRequest;
import com.mcly.grooming.api.CreateGroomingOrderRequest;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class GroomingCommandRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public GroomingCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long createOrder(CreateGroomingOrderRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into grooming_order (
                        store_id, member_id, pet_id, staff_id,
                        scheduled_at, status, total_fee, remark
                    ) values (?, ?, ?, ?, ?, 'BOOKED', ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, request.storeId());
            statement.setLong(2, request.memberId());
            statement.setLong(3, request.petId());
            if (request.staffId() == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, request.staffId());
            }
            if (request.scheduledAt() == null || request.scheduledAt().isBlank()) {
                statement.setNull(5, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.parse(request.scheduledAt(), DT_FORMATTER)));
            }
            statement.setBigDecimal(6, request.totalFee() != null ? request.totalFee() : BigDecimal.ZERO);
            statement.setString(7, request.remark());
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Transactional
    public void completeOrder(CompleteGroomingRequest request) {
        int updated = jdbcTemplate.update("""
                update grooming_order
                set status = 'COMPLETED',
                    completed_at = current_timestamp,
                    updated_at = current_timestamp
                where id = ? and status != 'COMPLETED'
                """, request.orderId());
        if (updated == 0) {
            throw new IllegalArgumentException("订单不存在或已完成");
        }
        if (request.note() != null && !request.note().isBlank()) {
            jdbcTemplate.update("""
                    insert into grooming_service_record (grooming_order_id, service_name, note)
                    values (?, '完工记录', ?)
                    """, request.orderId(), request.note());
        }
    }
}
