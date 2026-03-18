package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameContractReviewRepository extends JpaRepository<GameContractReview, Long> {

    Optional<GameContractReview> findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDesc(
        Integer gameSessionId,
        Long propertyId
    );
}
