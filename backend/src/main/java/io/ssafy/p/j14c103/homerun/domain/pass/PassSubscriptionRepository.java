package io.ssafy.p.j14c103.homerun.domain.pass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassSubscriptionRepository extends JpaRepository<PassSubscription, Long> {

    List<PassSubscription> findByUserIdAndIsActiveTrue(Long userId);

    List<PassSubscription> findAllByUserIdOrderBySubscribedAtAsc(Long userId);

    Optional<PassSubscription> findByIdAndUserId(Long id, Long userId);
}
