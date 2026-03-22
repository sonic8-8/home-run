package io.ssafy.p.j14c103.homerun.domain.paymenthistory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPaymentHistoryRepository extends JpaRepository<MemberPaymentHistory, Long> {

    List<MemberPaymentHistory> findAllByUserIdOrderByPaymentDateDescCreatedAtDesc(Long userId);
}
