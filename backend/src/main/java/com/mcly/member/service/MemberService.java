package com.mcly.member.service;

import com.mcly.member.api.CreateMemberRequest;
import com.mcly.member.api.MemberSummaryResponse;
import com.mcly.member.domain.Member;
import com.mcly.member.repository.MemberCommandRepository;
import com.mcly.member.repository.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCommandRepository memberCommandRepository;

    public MemberService(MemberRepository memberRepository, MemberCommandRepository memberCommandRepository) {
        this.memberRepository = memberRepository;
        this.memberCommandRepository = memberCommandRepository;
    }

    public List<MemberSummaryResponse> listMembers() {
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return List.of(
                    new MemberSummaryResponse(1L, "张三", "13900000001", "SILVER", true),
                    new MemberSummaryResponse(2L, "李四", "13900000002", "GOLD", true)
            );
        }
        return members.stream()
                .map(member -> new MemberSummaryResponse(
                        member.getId(),
                        member.getName(),
                        member.getPhone(),
                        member.getLevel(),
                        member.isFaceBound()
                ))
                .toList();
    }

    public Long createMember(CreateMemberRequest request) {
        return memberCommandRepository.create(request);
    }
}
