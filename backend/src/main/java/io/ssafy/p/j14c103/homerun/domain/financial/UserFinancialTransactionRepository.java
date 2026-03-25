package io.ssafy.p.j14c103.homerun.domain.financial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFinancialTransactionRepository extends JpaRepository<UserFinancialTransaction, Long> {
}
