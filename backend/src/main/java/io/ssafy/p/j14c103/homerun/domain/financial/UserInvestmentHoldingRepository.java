package io.ssafy.p.j14c103.homerun.domain.financial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInvestmentHoldingRepository extends JpaRepository<UserInvestmentHolding, Long> {

    List<UserInvestmentHolding> findByUserIdAndActiveYnTrue(Long userId);

    List<UserInvestmentHolding> findByUserFinancialProductIdAndActiveYnTrue(Long userFinancialProductId);
}
