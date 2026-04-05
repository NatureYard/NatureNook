package com.mcly.member.repository;

import com.mcly.member.api.CreateMemberRequest;
import com.mcly.member.domain.Member;
import org.springframework.stereotype.Repository;

@Repository
public class MemberCommandRepository {

    private final MemberRepository memberRepository;

    public MemberCommandRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Long create(CreateMemberRequest request) {
        Member member = new Member(request.name(), request.phone(), request.level());
        member.setStoreId(request.storeId());
        return memberRepository.save(member).getId();
    }
}
