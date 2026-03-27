package io.ssafy.p.j14c103.homerun.domain.user;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHomeCreditScoreSnapshotRepository extends JpaRepository<UserHomeCreditScoreSnapshot, Long> {

    Optional<UserHomeCreditScoreSnapshot> findByUserIdAndScoreMonthStart(Long userId, LocalDate scoreMonthStart);

    boolean existsByUserId(Long userId);
}
