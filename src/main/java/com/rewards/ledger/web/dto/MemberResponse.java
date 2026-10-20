package com.rewards.ledger.web.dto;

import com.rewards.ledger.domain.Member;
import java.time.Instant;

public record MemberResponse(Long memberId, String name, String email, Instant createdAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getCreatedAt());
    }
}
