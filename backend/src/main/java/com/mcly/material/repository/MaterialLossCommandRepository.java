package com.mcly.material.repository;

import com.mcly.material.api.ReportMaterialLossRequest;
import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MaterialLossCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialLossCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Long create(ReportMaterialLossRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into material_loss_order (
                        store_id, warehouse_id, staff_id, remark
                    ) values (?, ?, ?, ?)
                    """, new String[]{"id"});
            statement.setLong(1, request.storeId());
            statement.setLong(2, request.warehouseId());
            statement.setLong(3, request.staffId());
            statement.setString(4, request.remark());
            return statement;
        }, keyHolder);

        Long lossOrderId = keyHolder.getKey().longValue();
        jdbcTemplate.update("""
                insert into material_loss_item (
                    loss_order_id, material_item_id, quantity, unit, reason
                ) values (?, ?, ?, ?, ?)
                """, lossOrderId, request.materialItemId(), request.quantity(), request.unit(), request.reason());

        jdbcTemplate.update("""
                update material_stock
                set quantity = greatest(0, quantity - ?), updated_at = current_timestamp
                where warehouse_id = ? and material_item_id = ?
                """, request.quantity(), request.warehouseId(), request.materialItemId());

        return lossOrderId;
    }
}
