package com.rewards.ledger.web;

import com.rewards.ledger.domain.Member;
import com.rewards.ledger.service.MemberService;
import com.rewards.ledger.service.RewardService;
import com.rewards.ledger.web.dto.CreateMemberRequest;
import com.rewards.ledger.web.dto.MemberDetailsResponse;
import com.rewards.ledger.web.dto.MemberResponse;
import com.rewards.ledger.web.dto.RewardResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final RewardService rewardService;

    public MemberController(MemberService memberService, RewardService rewardService) {
        this.memberService = memberService;
        this.rewardService = rewardService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        Member member = memberService.create(request);
        return ResponseEntity
                .created(URI.create("/members/" + member.getId()))
                .body(MemberResponse.from(member));
    }

    @GetMapping("/{memberId}")
    public MemberDetailsResponse get(@PathVariable Long memberId) {
        return MemberDetailsResponse.from(memberService.getWithBalance(memberId));
    }

    @GetMapping("/{memberId}/rewards")
    public List<RewardResponse> rewards(@PathVariable Long memberId) {
        return rewardService.findByMember(memberId).stream()
                .map(RewardResponse::from)
                .toList();
    }
}
