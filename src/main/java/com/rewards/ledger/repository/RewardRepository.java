package com.rewards.ledger.repository;

import com.rewards.ledger.domain.Reward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByMemberIdOrderByEventDateAscIdAsc(Long memberId);

    @Query("select coalesce(sum(r.points), 0) from Reward r where r.memberId = :memberId")
    long sumPointsByMemberId(@Param("memberId") Long memberId);
}
