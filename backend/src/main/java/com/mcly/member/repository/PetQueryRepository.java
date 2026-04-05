package com.mcly.member.repository;

import com.mcly.common.repository.QuerySupport;
import com.mcly.member.api.PetSummaryResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PetQueryRepository extends QuerySupport {

    public PetQueryRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<PetSummaryResponse> listPets() {
        return query("""
                select id, name, species, breed, member_id
                from pet_profile
                order by id
                """, (rs, rowNum) -> new PetSummaryResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("species"),
                rs.getString("breed"),
                rs.getLong("member_id")
        ));
    }
}

