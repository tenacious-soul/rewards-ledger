package com.rewards.ledger.web;

import com.rewards.ledger.service.RewardService;
import com.rewards.ledger.web.dto.CreateRewardRequest;
import com.rewards.ledger.web.dto.RewardResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse create(@Valid @RequestBody CreateRewardRequest request) {
        return RewardResponse.from(rewardService.create(request));
    }
}
