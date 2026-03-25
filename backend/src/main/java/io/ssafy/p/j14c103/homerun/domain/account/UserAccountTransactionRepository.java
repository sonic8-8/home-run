package io.ssafy.p.j14c103.homerun.domain.account;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountTransactionRepository extends JpaRepository<UserAccountTransaction, Long> {

    List<UserAccountTransaction> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime createdAt);

    List<UserAccountTransaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
