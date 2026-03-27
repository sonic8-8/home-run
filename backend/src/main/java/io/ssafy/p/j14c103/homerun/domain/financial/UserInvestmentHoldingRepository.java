package io.ssafy.p.j14c103.homerun.domain.financial;

import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserInvestmentHoldingRepository extends JpaRepository<UserInvestmentHolding, Long> {

    List<UserInvestmentHolding> findByUserIdAndActiveYnTrue(Long userId);

    List<UserInvestmentHolding> findByUserFinancialProductIdAndActiveYnTrue(Long userFinancialProductId);

    void deleteAllByUserFinancialProductIdIn(Collection<Long> userFinancialProductIds);

    @Query("SELECT DISTINCT h.userId FROM UserInvestmentHolding h WHERE h.activeYn = true")
    List<Long> findDistinctUserIdsByActiveYnTrue();
}
