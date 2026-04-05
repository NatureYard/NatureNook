package com.mcly.dashboard.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.dashboard.api.DashboardSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardQueryRepository extends QuerySupport {

    public DashboardQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public DashboardSummaryResponse getSummary() {
        Long totalMembers = jdbcTemplate.queryForObject("select count(*) from member", Long.class);
        Long activeCards = jdbcTemplate.queryForObject("select count(*) from membership_card where status = 'ACTIVE'", Long.class);
        Long todayReservations = jdbcTemplate.queryForObject("select count(*) from reservation where reservation_date = current_date", Long.class);
        Long todayEntries = jdbcTemplate.queryForObject("select count(*) from entry_exit_record where direction = 'ENTRY' and occurred_at::date = current_date", Long.class);
        Long openRiskEvents = jdbcTemplate.queryForObject("select count(*) from risk_event where status = 'OPEN'", Long.class);
        Long lowStockItems = jdbcTemplate.queryForObject("""
                select count(*)
                from material_stock ms
                join material_item mi on mi.id = ms.material_item_id
                where ms.quantity < mi.safety_stock
                """, Long.class);

        return new DashboardSummaryResponse(
                defaultLong(totalMembers),
                defaultLong(activeCards),
                defaultLong(todayReservations),
                defaultLong(todayEntries),
                defaultLong(openRiskEvents),
                defaultLong(lowStockItems)
        );
    }

    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }
}

