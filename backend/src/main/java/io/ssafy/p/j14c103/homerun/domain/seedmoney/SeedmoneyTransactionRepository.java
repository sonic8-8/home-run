package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeedmoneyTransactionRepository extends JpaRepository<SeedmoneyTransaction, Long> {

    Page<SeedmoneyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<SeedmoneyTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
            Long userId,
            String transactionType,
            Pageable pageable
    );

    List<SeedmoneyTransaction> findByUserIdAndTransactionType(Long userId, String transactionType);

    List<SeedmoneyTransaction> findByUserIdAndTransactionTypeAndCreatedAtAfter(
            Long userId, String transactionType, LocalDateTime after);
}
