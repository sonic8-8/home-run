package io.ssafy.p.j14c103.homerun.domain.financial;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFinancialTransactionRepository extends JpaRepository<UserFinancialTransaction, Long> {

    void deleteAllByUserFinancialProductIdIn(Collection<Long> userFinancialProductIds);
}
