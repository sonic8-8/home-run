package io.ssafy.p.j14c103.homerun.domain.card;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    List<CardTransaction> findAllByUserIdOrderByPaymentDateDescCreatedAtDesc(Long userId);

    List<CardTransaction> findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
