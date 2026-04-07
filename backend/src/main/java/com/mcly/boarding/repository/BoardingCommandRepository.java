package com.mcly.boarding.repository;

import com.mcly.boarding.api.AddBoardingDailyRecordRequest;
import com.mcly.boarding.api.CreateBoardingOrderRequest;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BoardingCommandRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public BoardingCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long createOrder(CreateBoardingOrderRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into boarding_order (
                        store_id, member_id, pet_id, cage_no,
                        check_in_time, planned_check_out_time, status, remark
                    ) values (?, ?, ?, ?, ?, ?, 'CHECKED_IN', ?)
                    """, new String[]{"id"});
            statement.setLong(1, request.storeId());
            statement.setLong(2, request.memberId());
            statement.setLong(3, request.petId());
            statement.setString(4, request.cageNo());
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.parse(request.checkInTime(), DT_FORMATTER)));
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.parse(request.plannedCheckOutTime(), DT_FORMATTER)));
            statement.setString(7, request.remark());
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Transactional
    public Long addDailyRecord(AddBoardingDailyRecordRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into boarding_daily_record (
                        boarding_order_id, record_date, health_note, exception_note, staff_id
                    ) values (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, request.boardingOrderId());
            statement.setDate(2, Date.valueOf(LocalDate.parse(request.recordDate())));
            statement.setString(3, request.healthNote());
            statement.setString(4, request.exceptionNote());
            if (request.staffId() == null) {
                statement.setNull(5, Types.BIGINT);
            } else {
                statement.setLong(5, request.staffId());
            }
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
