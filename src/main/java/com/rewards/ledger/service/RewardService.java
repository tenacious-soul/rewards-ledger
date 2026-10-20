package com.rewards.ledger.service;

import com.rewards.ledger.domain.PointType;
import com.rewards.ledger.domain.Reward;
import com.rewards.ledger.exception.InsufficientBalanceException;
import com.rewards.ledger.exception.MemberNotFoundException;
import com.rewards.ledger.repository.MemberRepository;
import com.rewards.ledger.repository.RewardRepository;
import com.rewards.ledger.web.dto.CreateRewardRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RewardService {

    private final RewardRepository rewardRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public RewardService(RewardRepository rewardRepository, MemberRepository memberRepository, Clock clock) {
        this.rewardRepository = rewardRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public Reward create(CreateRewardRequest request) {
        PointType pointType = PointType.fromId(request.pointTypeId());
        Long memberId = request.memberId();
        memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (pointType.isDebit()) {
            ensureSufficientBalance(memberId, request.points());
        }

        Reward reward = new Reward(
                memberId,
                pointType,
                pointType.toSignedPoints(request.points()),
                request.description(),
                eventDateOrNow(request.eventDate()));
        return rewardRepository.save(reward);
    }

    public List<Reward> findByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId);
        }
        return rewardRepository.findByMemberIdOrderByEventDateAscIdAsc(memberId);
    }

    private void ensureSufficientBalance(Long memberId, long requestedPoints) {
        long balance = rewardRepository.sumPointsByMemberId(memberId);
        if (requestedPoints > balance) {
            throw new InsufficientBalanceException(balance, requestedPoints);
        }
    }

    private Instant eventDateOrNow(Instant eventDate) {
        return Optional.ofNullable(eventDate)
                .orElseGet(() -> Instant.now(clock))
                .truncatedTo(ChronoUnit.SECONDS);
    }
}
