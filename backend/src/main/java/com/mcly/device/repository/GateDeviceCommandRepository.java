package com.mcly.device.repository;

import com.mcly.device.api.GateVerifyRequest;
import java.sql.Types;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class GateDeviceCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public GateDeviceCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void updateHeartbeat(String deviceCode, String status) {
        jdbcTemplate.update("""
                update gate_device
                set last_seen_at = current_timestamp,
                    status = ?,
                    updated_at = current_timestamp
                where code = ?
                """, status != null ? status : "ONLINE", deviceCode);
    }

    @Transactional
    public void recordEntryExit(Long memberId, Long storeId, Long deviceId,
                                String direction, String result, boolean riskFlag, String reasonCode) {
        jdbcTemplate.update("""
                insert into entry_exit_record (
                    member_id, store_id, gate_device_id, direction, result, risk_flag, reason_code
                ) values (?, ?, ?, ?, ?, ?, ?)
                """,
                memberId, storeId, deviceId, direction, result, riskFlag, reasonCode);
    }
}
