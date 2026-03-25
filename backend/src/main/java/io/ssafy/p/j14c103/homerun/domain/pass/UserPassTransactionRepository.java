package io.ssafy.p.j14c103.homerun.domain.pass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPassTransactionRepository extends JpaRepository<UserPassTransaction, Long> {

    List<UserPassTransaction> findBySubscriptionIdAndTransactionDateAfter(Long subscriptionId, String transactionDate);
}
