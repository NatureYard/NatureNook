package com.mcly.customer.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerReservationResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerMiniQueryRepository extends QuerySupport {

    public CustomerMiniQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<CustomerOrderResponse> orders() {
        return query("""
                select id, order_no, order_type, status, paid_amount
                from customer_order
                order by id
                """, (rs, rowNum) -> new CustomerOrderResponse(
                rs.getLong("id"),
                rs.getString("order_no"),
                rs.getString("order_type"),
                rs.getString("status"),
                rs.getBigDecimal("paid_amount").toPlainString()
        ));
    }

    public List<CustomerCardResponse> listCards() {
        return query("""
                select mc.id,
                       m.name as member_name,
                       mc.card_type,
                       s.name as store_name,
                       mc.status,
                       to_char(mc.valid_from, 'YYYY-MM-DD') as valid_from,
                       to_char(mc.valid_to, 'YYYY-MM-DD') as valid_to
                from membership_card mc
                join member m on m.id = mc.member_id
                join store s on s.id = mc.store_id
                order by mc.id
                """, (rs, rowNum) -> new CustomerCardResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("card_type"),
                rs.getString("store_name"),
                rs.getString("status"),
                rs.getString("valid_from"),
                rs.getString("valid_to")
        ));
    }

    public List<CustomerPetResponse> listPets() {
        return query("""
                select p.id,
                       p.name,
                       p.species,
                       p.breed,
                       p.gender,
                       m.name as owner_name
                from pet_profile p
                join member m on m.id = p.member_id
                order by p.id
                """, (rs, rowNum) -> new CustomerPetResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("species"),
                rs.getString("breed"),
                rs.getString("gender"),
                rs.getString("owner_name")
        ));
    }

    public List<CustomerReservationResponse> listReservations() {
        return query("""
                select r.id,
                       m.name as member_name,
                       s.name as store_name,
                       r.reservation_type,
                       to_char(r.reservation_date, 'YYYY-MM-DD') as reservation_date,
                       r.time_slot,
                       r.status,
                       r.amount
                from reservation r
                join member m on m.id = r.member_id
                join store s on s.id = r.store_id
                order by r.reservation_date desc, r.id desc
                """, (rs, rowNum) -> new CustomerReservationResponse(
                rs.getLong("id"),
                rs.getString("member_name"),
                rs.getString("store_name"),
                rs.getString("reservation_type"),
                rs.getString("reservation_date"),
                rs.getString("time_slot"),
                rs.getString("status"),
                rs.getBigDecimal("amount").toPlainString()
        ));
    }
}


