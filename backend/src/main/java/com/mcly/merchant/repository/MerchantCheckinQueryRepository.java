package com.mcly.merchant.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.merchant.api.MerchantCheckinOrderResponse;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantCheckinQueryRepository extends QuerySupport {

    public MerchantCheckinQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<MerchantCheckinOrderResponse> listTodayOrders(Long storeId) {
        return query("""
                select co.id as order_id,
                       co.order_no,
                       co.member_id,
                       m.name as member_name,
                       coalesce(pp.name, '未关联宠物') as pet_name,
                       co.order_type,
                       co.status as order_status,
                       s.name as store_name,
                       to_char(r.reservation_date, 'YYYY-MM-DD') as reservation_date,
                       coalesce(r.time_slot, '') as time_slot,
                       m.face_bound,
                       exists(
                           select 1
                           from pass_entitlement pe
                           where pe.member_id = co.member_id
                             and pe.store_id = co.store_id
                             and pe.status = 'ACTIVE'
                             and pe.valid_from <= current_timestamp
                             and pe.valid_to >= current_timestamp
                       ) as active_entitlement,
                       coalesce((
                           select pe.source_type
                           from pass_entitlement pe
                           where pe.member_id = co.member_id
                             and pe.store_id = co.store_id
                             and pe.status = 'ACTIVE'
                             and pe.valid_from <= current_timestamp
                             and pe.valid_to >= current_timestamp
                           order by pe.valid_to desc
                           limit 1
                       ), '') as entitlement_name,
                       coalesce((
                           select to_char(pe.valid_to, 'YYYY-MM-DD"T"HH24:MI:SS')
                           from pass_entitlement pe
                           where pe.member_id = co.member_id
                             and pe.store_id = co.store_id
                             and pe.status = 'ACTIVE'
                             and pe.valid_from <= current_timestamp
                             and pe.valid_to >= current_timestamp
                           order by pe.valid_to desc
                           limit 1
                       ), '') as entitlement_valid_to
                from customer_order co
                join member m on m.id = co.member_id
                join store s on s.id = co.store_id
                left join reservation r on r.id = co.reservation_id
                left join pet_profile pp on pp.id = r.pet_id
                where co.store_id = ?
                  and (r.reservation_date = current_date or co.order_type in ('YEAR_CARD', 'MONTH_CARD', 'SEASON_CARD'))
                order by co.id desc
                limit 20
                """, (rs, rowNum) -> new MerchantCheckinOrderResponse(
                rs.getLong("order_id"),
                rs.getString("order_no"),
                rs.getLong("member_id"),
                rs.getString("member_name"),
                rs.getString("pet_name"),
                rs.getString("order_type"),
                rs.getString("order_status"),
                rs.getString("store_name"),
                rs.getString("reservation_date"),
                rs.getString("time_slot"),
                rs.getBoolean("face_bound"),
                rs.getBoolean("active_entitlement"),
                rs.getString("entitlement_name"),
                rs.getString("entitlement_valid_to")
        ), storeId);
    }

    public Map<String, Object> findOrderById(Long orderId) {
        var rows = jdbcTemplate.queryForList("""
                select co.id as order_id,
                       co.order_no,
                       co.member_id,
                       co.store_id,
                       co.status as order_status,
                       m.name as member_name
                from customer_order co
                join member m on m.id = co.member_id
                where co.id = ?
                """, orderId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
