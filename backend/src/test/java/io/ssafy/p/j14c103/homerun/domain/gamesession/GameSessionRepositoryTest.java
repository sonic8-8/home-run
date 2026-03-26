package io.ssafy.p.j14c103.homerun.domain.gamesession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameSessionRepositoryTest {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("GameSession을 저장하면 세션 메타데이터와 기본 진행 상태를 다시 조회할 수 있다.")
    @Test
    void saveGameSession() {
        // given
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );

        // when
        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);
        entityManager.clear();
        final GameSession found = gameSessionRepository.findById(saved.getGameSessionId())
            .orElseThrow();

        // then
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getSlotNumber()).isEqualTo(1);
        assertThat(found.getCharacterName()).isEqualTo("윤서");
        assertThat(found.getCharacterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(found.getJobType()).isEqualTo(JobType.STARTUP);
        assertThat(found.getHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(found.getRegionCode()).isEqualTo("11");
        assertThat(found.getDistrictCode()).isEqualTo("11680");
        assertThat(found.getTargetPropertyId()).isEqualTo(101L);
        assertThat(found.getDataSourceType()).isEqualTo(DataSourceType.PROFILE);
        assertThat(found.getCurrentTurn()).isEqualTo(0);
        assertThat(found.getCurrentDate()).isNull();
        assertThat(found.getCyclePhase()).isNull();
        assertThat(found.getCycleType()).isNull();
        assertThat(found.getCycleRemainingTurns()).isNull();
        assertThat(found.getCashBalance()).isEqualTo(Money.zero());
        assertThat(found.getNetWorth()).isEqualTo(Money.zero());
        assertThat(found.getSessionStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getLastPlayedAt()).isNull();
    }

    @DisplayName("같은 사용자의 같은 슬롯 번호는 중복 저장할 수 없다.")
    @Test
    void saveDuplicateSlotBySameUser() {
        // given
        gameSessionRepository.saveAndFlush(GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        ));

        final GameSession duplicate = GameSession.create(
            1L,
            1,
            "도윤",
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11710",
            202L,
            DataSourceType.MY_DATA
        );

        // when & then
        assertThatThrownBy(() -> gameSessionRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("다른 사용자라면 같은 슬롯 번호를 사용할 수 있다.")
    @Test
    void saveSameSlotByDifferentUsers() {
        // given
        gameSessionRepository.saveAndFlush(GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        ));

        final GameSession otherUserSession = GameSession.create(
            2L,
            1,
            "도윤",
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11710",
            202L,
            DataSourceType.MY_DATA
        );

        // when
        final GameSession saved = gameSessionRepository.saveAndFlush(otherUserSession);

        // then
        assertThat(saved.getGameSessionId()).isNotNull();
        assertThat(gameSessionRepository.count()).isEqualTo(2);
    }

    @DisplayName("사용자와 슬롯 번호로 세션 존재 여부를 조회할 수 있다.")
    @Test
    void existsByUserIdAndSlotNumber() {
        // given
        final GameSession saved = gameSessionRepository.saveAndFlush(GameSession.create(
            3L,
            2,
            "민지",
            CharacterType.FEMALE,
            JobType.MID_BIZ,
            HousingType.VILLA,
            "24",
            "24110",
            303L,
            DataSourceType.PROFILE
        ));
        entityManager.clear();

        // when
        final boolean exists = gameSessionRepository.existsByUserIdAndSlotNumber(3L, 2);
        final boolean notExists = gameSessionRepository.existsByUserIdAndSlotNumber(3L, 3);

        // then
        assertThat(saved.getGameSessionId()).isNotNull();
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @DisplayName("사용자 기준 세션 목록을 슬롯 번호 오름차순으로 조회할 수 있다.")
    @Test
    void findAllByUserIdOrderBySlotNumberAsc() {
        // given
        gameSessionRepository.saveAndFlush(GameSession.create(
            7L,
            3,
            "세번째",
            CharacterType.FEMALE,
            JobType.MID_BIZ,
            HousingType.VILLA,
            "11",
            "11680",
            303L,
            DataSourceType.PROFILE
        ));
        gameSessionRepository.saveAndFlush(GameSession.create(
            7L,
            1,
            "첫번째",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        ));
        gameSessionRepository.saveAndFlush(GameSession.create(
            8L,
            2,
            "다른유저",
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11710",
            202L,
            DataSourceType.MY_DATA
        ));
        entityManager.clear();

        // when
        final var found = gameSessionRepository.findAllByUserIdOrderBySlotNumberAsc(7L);

        // then
        assertThat(found)
            .extracting(GameSession::getSlotNumber, GameSession::getCharacterName)
            .containsExactly(
                tuple(1, "첫번째"),
                tuple(3, "세번째")
            );
    }

    @DisplayName("세션 진행 상태와 경제 사이클은 초기화 이후 값으로 갱신할 수 있다.")
    @Test
    void initializeCapitalAndAdvanceTurn() {
        // given
        final GameSession gameSession = GameSession.create(
            4L,
            3,
            "세준",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.NONE,
            "11",
            "11500",
            404L,
            DataSourceType.MY_DATA
        );
        gameSession.initializeCapital(
            Money.of(13_000_000L),
            Money.of(13_000_000L),
            LocalDate.of(2026, 1, 1),
            CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 4)
        );
        gameSession.advanceTurn(
            1,
            LocalDate.of(2026, 2, 1),
            Money.of(14_500_000L),
            Money.of(15_000_000L),
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 24)
        );

        // when
        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);
        entityManager.clear();
        final GameSession found = gameSessionRepository.findById(saved.getGameSessionId())
            .orElseThrow();

        // then
        assertThat(found.getCurrentTurn()).isEqualTo(1);
        assertThat(found.getCurrentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(found.getCyclePhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(found.getCycleType()).isEqualTo(CycleType.CYCLE_BOOM);
        assertThat(found.getCycleRemainingTurns()).isEqualTo(24);
        assertThat(found.getCashBalance()).isEqualTo(Money.of(14_500_000L));
        assertThat(found.getNetWorth()).isEqualTo(Money.of(15_000_000L));
        assertThat(found.getSessionStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    }
}
