package com.rewards.ledger.web.dto;

import com.rewards.ledger.domain.Reward;
import java.time.Instant;

public record RewardResponse(
        Long rewardId,
        Long memberId,
        int pointTypeId,
        long points,
        String description,
        Instant eventDate) {

    public static RewardResponse from(Reward reward) {
        return new RewardResponse(
                reward.getId(),
                reward.getMemberId(),
                reward.getPointType().getId(),
                reward.getPoints(),
                reward.getDescription(),
                reward.getEventDate());
    }
}
