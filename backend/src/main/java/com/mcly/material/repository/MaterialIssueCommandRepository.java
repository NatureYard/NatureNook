package com.mcly.material.repository;

import com.mcly.material.api.CreateMaterialIssueRequest;
import java.sql.PreparedStatement;
import java.sql.Types;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MaterialIssueCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialIssueCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long create(CreateMaterialIssueRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into material_issue_order (
                        store_id, warehouse_id, staff_id, biz_type, biz_id, remark
                    ) values (?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, request.storeId());
            statement.setLong(2, request.warehouseId());
            statement.setLong(3, request.staffId());
            statement.setString(4, request.bizType());
            if (request.bizId() == null) {
                statement.setNull(5, Types.BIGINT);
            } else {
                statement.setLong(5, request.bizId());
            }
            statement.setString(6, request.remark());
            return statement;
        }, keyHolder);

        Long issueOrderId = keyHolder.getKey().longValue();
        jdbcTemplate.update("""
                insert into material_issue_item (
                    issue_order_id, material_item_id, quantity, unit
                ) values (?, ?, ?, ?)
                """, issueOrderId, request.materialItemId(), request.quantity(), request.unit());

        int updated = jdbcTemplate.update("""
                update material_stock
                set quantity = quantity - ?, updated_at = current_timestamp
                where warehouse_id = ? and material_item_id = ? and quantity >= ?
                """, request.quantity(), request.warehouseId(), request.materialItemId(), request.quantity());
        if (updated == 0) {
            throw new IllegalArgumentException("库存不足或库存记录不存在");
        }

        jdbcTemplate.update("""
                insert into material_consumption_record (
                    store_id, material_item_id, biz_type, biz_id, staff_id, quantity
                ) values (?, ?, ?, ?, ?, ?)
                """,
                request.storeId(),
                request.materialItemId(),
                request.bizType(),
                request.bizId(),
                request.staffId(),
                request.quantity()
        );
        return issueOrderId;
    }
}

