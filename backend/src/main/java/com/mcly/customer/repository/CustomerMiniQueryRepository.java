package com.mcly.customer.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.customer.api.CustomerOrderResponse;
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
}

