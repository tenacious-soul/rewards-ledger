package com.rewards.ledger.service;

import com.rewards.ledger.domain.Member;
import com.rewards.ledger.domain.MemberBalance;
import com.rewards.ledger.exception.DuplicateEmailException;
import com.rewards.ledger.exception.MemberNotFoundException;
import com.rewards.ledger.repository.MemberRepository;
import com.rewards.ledger.repository.RewardRepository;
import com.rewards.ledger.web.dto.CreateMemberRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RewardRepository rewardRepository;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, RewardRepository rewardRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.rewardRepository = rewardRepository;
        this.clock = clock;
    }

    @Transactional
    public Member create(CreateMemberRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
        return memberRepository.save(new Member(request.name().trim(), email, now));
    }

    public MemberBalance getWithBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return new MemberBalance(member, rewardRepository.sumPointsByMemberId(memberId));
    }
}
