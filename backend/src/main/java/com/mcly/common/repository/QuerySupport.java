package com.mcly.common.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class QuerySupport {

    protected final JdbcTemplate jdbcTemplate;

    protected QuerySupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected boolean hasAny(String sql) {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result != null && result > 0;
    }

    protected <T> List<T> query(String sql, org.springframework.jdbc.core.RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }
}

