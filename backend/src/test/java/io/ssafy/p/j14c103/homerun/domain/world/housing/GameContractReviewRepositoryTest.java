package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

// 세션에 대한 매물, 함정, 실제 함정, 최종 판정, 검토 이력/결과 테스트
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameContractReviewRepositoryTest {

    @Autowired
    private GameContractReviewRepository gameContractReviewRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("GameContractReview를 저장하면 검토 상태와 함정 선택 결과를 다시 조회할 수 있다.")
    @Test
    void saveGameContractReview() {
        // given
        GameContractReview review = GameContractReview.create(
                1L,
                101L,
                ContractReviewStatus.PASSED,
                List.of("TRAP-01", "TRAP-02"),
                List.of("TRAP-01", "TRAP-02"),
                ContractResult.SAFE,
                LocalDateTime.of(2026, 3, 16, 10, 0)
        );

        // when
        GameContractReview saved =
                gameContractReviewRepository.saveAndFlush(review);
        entityManager.clear();

        GameContractReview found =
                gameContractReviewRepository.findById(saved.getId())
                        .orElseThrow();

        // then
        assertThat(found.getGameSessionId()).isEqualTo(1L);
        assertThat(found.getPropertyId()).isEqualTo(101L);
        assertThat(found.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(found.getCheckedTraps()).containsExactly("TRAP-01", "TRAP-02");
        assertThat(found.getDetectedTraps()).containsExactly("TRAP-01", "TRAP-02");
        assertThat(found.getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(found.getReviewedAt()).isEqualTo(LocalDateTime.of(2026, 3, 16,
                10, 0));
    }

    @DisplayName("세션과 매물 기준 최신 계약 검토 결과를 조회할 수 있다.")
    @Test
    void findLatestReviewByGameSessionIdAndPropertyId() {
        // given
        GameContractReview older = GameContractReview.create(
                1L,
                101L,
                ContractReviewStatus.FAILED,
                List.of("TRAP-01"),
                List.of("TRAP-01", "TRAP-02"),
                ContractResult.FAIL,
                LocalDateTime.of(2026, 3, 16, 9, 0)
        );

        GameContractReview latest = GameContractReview.create(
                1L,
                101L,
                ContractReviewStatus.PASSED,
                List.of("TRAP-01", "TRAP-02"),
                List.of("TRAP-01", "TRAP-02"),
                ContractResult.SAFE,
                LocalDateTime.of(2026, 3, 16, 10, 0)
        );

        gameContractReviewRepository.saveAndFlush(older);
        gameContractReviewRepository.saveAndFlush(latest);
        entityManager.clear();

        // when
        Optional<GameContractReview> result =
                gameContractReviewRepository.findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDesc(
                        1L,
                        101L
                );

        // then
        assertThat(result).isPresent();

        assertThat(result.orElseThrow().getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);

        assertThat(result.orElseThrow().getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(result.orElseThrow().getReviewedAt())
                .isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 0));
    }

    @DisplayName("reviewedAt이 같으면 id 내림차순으로 최신 리뷰를 조회한다.")
    @Test
    void findLatestReviewByGameSessionIdAndPropertyIdWithIdTieBreaker() {
        GameContractReview first = GameContractReview.create(
                1L,
                101L,
                ContractReviewStatus.FAILED,
                List.of("TRAP-01"),
                List.of("TRAP-01"),
                ContractResult.FAIL,
                LocalDateTime.of(2026, 3, 16, 10, 0)
        );

        GameContractReview second = GameContractReview.create(
                1L,
                101L,
                ContractReviewStatus.PASSED,
                List.of("TRAP-01", "TRAP-02"),
                List.of("TRAP-01", "TRAP-02"),
                ContractResult.SAFE,
                LocalDateTime.of(2026, 3, 16, 10, 0)
        );

        gameContractReviewRepository.saveAndFlush(first);
        gameContractReviewRepository.saveAndFlush(second);
        entityManager.clear();

        Optional<GameContractReview> result =
                gameContractReviewRepository
                    .findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDescIdDesc(1L, 101L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(result.orElseThrow().getContractResult()).isEqualTo(ContractResult.SAFE);
    }
}
