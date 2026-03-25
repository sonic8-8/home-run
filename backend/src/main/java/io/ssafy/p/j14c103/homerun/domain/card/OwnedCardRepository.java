package io.ssafy.p.j14c103.homerun.domain.card;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnedCardRepository extends JpaRepository<OwnedCard, Long> {

    List<OwnedCard> findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(Long userId);

    boolean existsByUserIdAndActiveYnTrue(Long userId);
}
