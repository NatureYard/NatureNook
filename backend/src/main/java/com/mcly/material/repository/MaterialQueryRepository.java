package com.mcly.material.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.material.api.MaterialCategoryResponse;
import com.mcly.material.api.MaterialStockResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MaterialQueryRepository extends QuerySupport {

    public MaterialQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<MaterialCategoryResponse> listCategories() {
        return query("""
                select code, name, material_scope
                from material_category
                order by id
                """, (rs, rowNum) -> new MaterialCategoryResponse(
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("material_scope")
        ));
    }

    public List<MaterialStockResponse> listStocks() {
        return query("""
                select mi.id,
                       mi.name,
                       mc.name as category_name,
                       mi.unit,
                       ms.quantity,
                       mi.safety_stock
                from material_stock ms
                join material_item mi on mi.id = ms.material_item_id
                join material_category mc on mc.id = mi.category_id
                order by mi.id
                """, (rs, rowNum) -> {
            double quantity = rs.getDouble("quantity");
            double safetyStock = rs.getDouble("safety_stock");
            return new MaterialStockResponse(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getString("unit"),
                    quantity,
                    safetyStock,
                    quantity < safetyStock
            );
        });
    }
}

