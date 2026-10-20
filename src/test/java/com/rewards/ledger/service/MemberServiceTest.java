package com.rewards.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rewards.ledger.domain.Member;
import com.rewards.ledger.domain.MemberBalance;
import com.rewards.ledger.exception.DuplicateEmailException;
import com.rewards.ledger.exception.MemberNotFoundException;
import com.rewards.ledger.repository.MemberRepository;
import com.rewards.ledger.repository.RewardRepository;
import com.rewards.ledger.web.dto.CreateMemberRequest;
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
class MemberServiceTest {

    private static final Instant NOW = Instant.parse("2024-01-15T09:00:00Z");

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RewardRepository rewardRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.plusMillis(123), ZoneOffset.UTC);
        memberService = new MemberService(memberRepository, rewardRepository, clock);
    }

    @Test
    void createsMemberWithNormalizedEmailAndCurrentTimestamp() {
        when(memberRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member member = memberService.create(new CreateMemberRequest(" Alice Johnson ", " Alice@Example.com "));

        assertThat(member.getName()).isEqualTo("Alice Johnson");
        assertThat(member.getEmail()).isEqualTo("alice@example.com");
        assertThat(member.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(memberRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(new CreateMemberRequest("Alice", "alice@example.com")))
                .isInstanceOf(DuplicateEmailException.class);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void returnsMemberWithBalance() {
        Member member = new Member("Alice", "alice@example.com", NOW);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(rewardRepository.sumPointsByMemberId(1L)).thenReturn(450L);

        MemberBalance result = memberService.getWithBalance(1L);

        assertThat(result.member()).isSameAs(member);
        assertThat(result.points()).isEqualTo(450L);
    }

    @Test
    void failsWhenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getWithBalance(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
