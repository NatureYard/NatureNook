package com.mcly.customer.repository;

import com.mcly.common.auth.AuthContext;
import com.mcly.common.repository.QuerySupport;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerContextResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPassResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerProfileResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerMiniQueryRepository extends QuerySupport {

    public CustomerMiniQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public CustomerContextResponse context() {
        CurrentCustomer currentCustomer = currentCustomer();
        if (currentCustomer == null) {
            return new CustomerContextResponse(null, "当前会员", "NORMAL", null, "未分配门店", List.of(), List.of());
        }
        return new CustomerContextResponse(
                currentCustomer.memberId(),
                currentCustomer.memberName(),
                currentCustomer.memberLevel(),
                currentCustomer.storeId(),
                currentCustomer.storeName(),
                pets(currentCustomer.memberId()),
                cards(currentCustomer.memberId())
        );
    }

    public List<CustomerOrderResponse> orders() {
        CurrentCustomer currentCustomer = currentCustomer();
        return currentCustomer == null ? List.of() : orders(currentCustomer.memberId());
    }

    public List<CustomerPetResponse> pets() {
        CurrentCustomer currentCustomer = currentCustomer();
        return currentCustomer == null ? List.of() : pets(currentCustomer.memberId());
    }

    public List<CustomerPassResponse> passes() {
        CurrentCustomer currentCustomer = currentCustomer();
        return currentCustomer == null ? List.of() : passes(currentCustomer.memberId());
    }

    public List<CustomerCardResponse> cards() {
        CurrentCustomer currentCustomer = currentCustomer();
        return currentCustomer == null ? List.of() : cards(currentCustomer.memberId());
    }

    public CustomerProfileResponse profile() {
        CurrentCustomer currentCustomer = currentCustomer();
        if (currentCustomer == null) {
            return new CustomerProfileResponse(
                    "当前会员",
                    "NORMAL",
                    "未分配门店",
                    List.of("会员等级：NORMAL", "活跃卡种：0", "累计订单：0", "人脸录入状态：未录入")
            );
        }
        Integer activeCardCount = jdbcTemplate.queryForObject(
                "select count(*) from membership_card where member_id = ? and status = 'ACTIVE'",
                Integer.class,
                currentCustomer.memberId()
        );
        Integer orderCount = jdbcTemplate.queryForObject(
                "select count(*) from customer_order where member_id = ?",
                Integer.class,
                currentCustomer.memberId()
        );
        return new CustomerProfileResponse(
                currentCustomer.memberName(),
                currentCustomer.memberLevel(),
                currentCustomer.storeName(),
                List.of(
                        "会员等级：" + currentCustomer.memberLevel(),
                        "活跃卡种：" + (activeCardCount == null ? 0 : activeCardCount),
                        "累计订单：" + (orderCount == null ? 0 : orderCount),
                        "人脸录入状态：" + (currentCustomer.faceBound() ? "已录入" : "未录入")
                )
        );
    }

    private List<CustomerOrderResponse> orders(Long memberId) {
        return query("""
                select co.id,
                       co.order_no,
                       co.order_type,
                       co.status,
                       co.paid_amount,
                       s.name as store_name,
                       to_char(r.reservation_date, 'YYYY-MM-DD') as reservation_date,
                       coalesce(r.time_slot, '') as time_slot,
                       pe.id as pass_entitlement_id
                from customer_order co
                join store s on s.id = co.store_id
                left join reservation r on r.id = co.reservation_id
                left join pass_entitlement pe on pe.source_type in ('DAY_TICKET','GROOMING_PACKAGE','BOARDING_DAY')
                     and pe.source_id = r.id and pe.member_id = co.member_id
                where co.member_id = ?
                order by co.id desc
                """, (rs, rowNum) -> new CustomerOrderResponse(
                rs.getLong("id"),
                rs.getString("order_no"),
                toOrderName(rs.getString("order_type")),
                rs.getString("status"),
                rs.getBigDecimal("paid_amount").toPlainString(),
                rs.getString("store_name"),
                rs.getString("reservation_date"),
                rs.getString("time_slot"),
                rs.getObject("pass_entitlement_id", Long.class)
        ), memberId);
    }

    private List<CustomerPassResponse> passes(Long memberId) {
        return query("""
                select pe.id,
                       pe.source_type,
                       pe.status,
                       s.name as store_name,
                       to_char(pe.valid_from, 'YYYY-MM-DD"T"HH24:MI:SS') as valid_from,
                       to_char(pe.valid_to, 'YYYY-MM-DD"T"HH24:MI:SS') as valid_to,
                       pe.reentry_policy
                from pass_entitlement pe
                join store s on s.id = pe.store_id
                where pe.member_id = ?
                  and pe.status = 'ACTIVE'
                  and pe.valid_to >= current_timestamp
                order by pe.valid_to desc
                """, (rs, rowNum) -> new CustomerPassResponse(
                rs.getLong("id"),
                toEntitlementName(rs.getString("source_type")),
                rs.getString("status"),
                rs.getString("store_name"),
                rs.getString("valid_from"),
                rs.getString("valid_to"),
                rs.getString("reentry_policy")
        ), memberId);
    }

    private List<CustomerPetResponse> pets(Long memberId) {
        return query("""
                select id,
                       name,
                       species,
                       coalesce(breed, '') as breed,
                       coalesce(gender, '') as gender,
                       coalesce(weight::text, '') as weight
                from pet_profile
                where member_id = ?
                order by id
                """, (rs, rowNum) -> new CustomerPetResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("species"),
                rs.getString("breed"),
                rs.getString("gender"),
                rs.getString("weight")
        ), memberId);
    }

    private List<CustomerCardResponse> cards(Long memberId) {
        return query("""
                select mc.id,
                       mc.card_type,
                       mc.status,
                       to_char(mc.valid_to, 'YYYY-MM-DD') as valid_to
                from membership_card mc
                where mc.member_id = ?
                order by mc.id
                """, (rs, rowNum) -> {
            String cardType = rs.getString("card_type");
            return new CustomerCardResponse(
                    rs.getLong("id"),
                    toCardName(cardType),
                    toCardDesc(cardType),
                    toCardPrice(cardType),
                    rs.getString("status"),
                    rs.getString("valid_to")
            );
        }, memberId);
    }

    private CurrentCustomer currentCustomer() {
        // 优先从认证上下文获取当前会员（登录后由 AuthInterceptor 写入）
        Long authMemberId = AuthContext.getMemberId();
        if (authMemberId != null) {
            return currentCustomerById(authMemberId);
        }
        // 降级：未登录时取第一个会员（开发兼容，正式环境应返回 null）
        return jdbcTemplate.query("""
                select m.id,
                       m.name,
                       m.level,
                       m.face_bound,
                       s.id as store_id,
                       coalesce(s.name, '未分配门店') as store_name
                from member m
                left join store s on s.id = m.store_id
                order by m.id
                limit 1
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new CurrentCustomer(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("level"),
                    rs.getObject("store_id", Long.class),
                    rs.getString("store_name"),
                    rs.getBoolean("face_bound")
            );
        });
    }

    private CurrentCustomer currentCustomerById(Long memberId) {
        return jdbcTemplate.query("""
                select m.id,
                       m.name,
                       m.level,
                       m.face_bound,
                       s.id as store_id,
                       coalesce(s.name, '未分配门店') as store_name
                from member m
                left join store s on s.id = m.store_id
                where m.id = ?
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new CurrentCustomer(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("level"),
                    rs.getObject("store_id", Long.class),
                    rs.getString("store_name"),
                    rs.getBoolean("face_bound")
            );
        }, memberId);
    }

    private String toCardName(String cardType) {
        return switch (cardType) {
            case "MONTH_CARD" -> "月卡";
            case "SEASON_CARD" -> "季卡";
            case "YEAR_CARD" -> "年卡";
            default -> cardType;
        };
    }

    private String toCardDesc(String cardType) {
        return switch (cardType) {
            case "MONTH_CARD" -> "30 天内多次入园";
            case "SEASON_CARD" -> "90 天内多次入园";
            case "YEAR_CARD" -> "365 天内多次入园";
            default -> "会员有效期内可按规则使用";
        };
    }

    private String toCardPrice(String cardType) {
        return switch (cardType) {
            case "MONTH_CARD" -> "399";
            case "SEASON_CARD" -> "999";
            case "YEAR_CARD" -> "1288";
            default -> "0";
        };
    }

    private String toOrderName(String orderType) {
        return switch (orderType) {
            case "TICKET" -> "单次门票";
            case "GROOMING" -> "洗护套餐";
            case "BOARDING" -> "寄养预约";
            case "YEAR_CARD" -> "年卡";
            case "MONTH_CARD" -> "月卡";
            case "SEASON_CARD" -> "季卡";
            default -> orderType;
        };
    }

    private String toEntitlementName(String sourceType) {
        return switch (sourceType) {
            case "DAY_TICKET" -> "单次门票入园资格";
            case "GROOMING_PACKAGE" -> "洗护到店资格";
            case "BOARDING_DAY" -> "寄养到店资格";
            case "YEAR_CARD" -> "年卡入园资格";
            case "MONTH_CARD" -> "月卡入园资格";
            case "SEASON_CARD" -> "季卡入园资格";
            default -> sourceType;
        };
    }

    private record CurrentCustomer(
            Long memberId,
            String memberName,
            String memberLevel,
            Long storeId,
            String storeName,
            boolean faceBound
    ) {
    }
}
