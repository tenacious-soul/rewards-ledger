package com.rewards.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rewards.ledger.domain.Member;
import com.rewards.ledger.domain.PointType;
import com.rewards.ledger.domain.Reward;
import com.rewards.ledger.exception.InsufficientBalanceException;
import com.rewards.ledger.exception.InvalidPointTypeException;
import com.rewards.ledger.exception.MemberNotFoundException;
import com.rewards.ledger.repository.MemberRepository;
import com.rewards.ledger.repository.RewardRepository;
import com.rewards.ledger.web.dto.CreateRewardRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    private static final Instant NOW = Instant.parse("2024-02-01T14:22:10Z");
    private static final long MEMBER_ID = 1L;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private MemberRepository memberRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(rewardRepository, memberRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void recordsCreditAsPositivePointsDatedNow() {
        givenMemberExists();
        whenSavingReturnArgument();

        Reward reward = rewardService.create(request(PointType.PURCHASE_EARNING, 500));

        assertThat(reward.getPoints()).isEqualTo(500);
        assertThat(reward.getPointType()).isEqualTo(PointType.PURCHASE_EARNING);
        assertThat(reward.getEventDate()).isEqualTo(NOW);
    }

    @Test
    void recordsRedemptionAsNegativePointsWhenBalanceIsSufficient() {
        givenMemberExists();
        whenSavingReturnArgument();
        when(rewardRepository.sumPointsByMemberId(MEMBER_ID)).thenReturn(300L);

        Reward reward = rewardService.create(request(PointType.REDEMPTION, 300));

        assertThat(reward.getPoints()).isEqualTo(-300);
    }

    @Test
    void rejectsRedemptionExceedingBalance() {
        givenMemberExists();
        when(rewardRepository.sumPointsByMemberId(MEMBER_ID)).thenReturn(299L);

        assertThatThrownBy(() -> rewardService.create(request(PointType.REDEMPTION, 300)))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void keepsProvidedEventDate() {
        givenMemberExists();
        whenSavingReturnArgument();
        Instant eventDate = Instant.parse("2024-02-05T10:15:33Z");
        CreateRewardRequest request = new CreateRewardRequest(MEMBER_ID, 2, 200L, "Referred user Bob", eventDate);

        Reward reward = rewardService.create(request);

        assertThat(reward.getEventDate()).isEqualTo(eventDate);
    }

    @Test
    void rejectsUnknownMember() {
        when(memberRepository.findByIdForUpdate(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.create(request(PointType.CASHBACK, 50)))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void rejectsUnknownPointType() {
        CreateRewardRequest request = new CreateRewardRequest(MEMBER_ID, 9, 50L, null, null);

        assertThatThrownBy(() -> rewardService.create(request))
                .isInstanceOf(InvalidPointTypeException.class);
    }

    @Test
    void listingRewardsOfUnknownMemberFails() {
        when(memberRepository.existsById(MEMBER_ID)).thenReturn(false);

        assertThatThrownBy(() -> rewardService.findByMember(MEMBER_ID))
                .isInstanceOf(MemberNotFoundException.class);
    }

    private void givenMemberExists() {
        Member member = new Member("Alice", "alice@example.com", NOW);
        when(memberRepository.findByIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(member));
    }

    private void whenSavingReturnArgument() {
        when(rewardRepository.save(any(Reward.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static CreateRewardRequest request(PointType type, long points) {
        return new CreateRewardRequest(MEMBER_ID, type.getId(), points, type.getDescription(), null);
    }
}
