package com.rewards.ledger.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateRewardRequest(
        @NotNull Long memberId,
        @NotNull Integer pointTypeId,
        @NotNull @Positive Long points,
        @Size(max = 255) String description,
        Instant eventDate) {
}
