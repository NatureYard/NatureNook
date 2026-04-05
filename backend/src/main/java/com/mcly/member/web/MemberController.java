package com.mcly.member.web;

import com.mcly.common.api.ApiResponse;
import com.mcly.common.api.IdResponse;
import com.mcly.member.api.CreateMemberRequest;
import com.mcly.member.api.MemberSummaryResponse;
import com.mcly.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<List<MemberSummaryResponse>> list() {
        return ApiResponse.ok(memberService.listMembers());
    }

    @PostMapping
    public ApiResponse<IdResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        return ApiResponse.ok(new IdResponse(memberService.createMember(request)));
    }
}
