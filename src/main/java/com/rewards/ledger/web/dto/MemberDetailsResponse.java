package com.rewards.ledger.web.dto;

import com.rewards.ledger.domain.Member;
import com.rewards.ledger.domain.MemberBalance;
import java.time.Instant;

public record MemberDetailsResponse(
        Long memberId,
        String name,
        String email,
        long pointsBalance,
        Instant createdAt) {

    public static MemberDetailsResponse from(MemberBalance balance) {
        Member member = balance.member();
        return new MemberDetailsResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                balance.points(),
                member.getCreatedAt());
    }
}
