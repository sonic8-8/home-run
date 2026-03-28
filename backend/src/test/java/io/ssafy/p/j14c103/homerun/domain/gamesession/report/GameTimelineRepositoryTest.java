package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
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
class GameTimelineRepositoryTest {

    @Autowired
    private GameTimelineRepository gameTimelineRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("game_timelines는 세션 기준 턴 번호 오름차순으로 조회된다")
    @Test
    void findAllByGameSessionIdOrderByTurnNumberAsc() {
        // given
        final GameSession gameSession = saveGameSession(31L, 1);
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            gameSession.getGameSessionId(),
            2,
            LocalDate.of(2026, 4, 1),
            14_500_000,
            18_000_000,
            20_000_000,
            4_000_000,
            2_000_000,
            3_500_000
        ));
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            gameSession.getGameSessionId(),
            1,
            LocalDate.of(2026, 3, 1),
            13_000_000,
            16_500_000,
            18_000_000,
            3_000_000,
            1_500_000,
            3_200_000
        ));
        entityManager.clear();

        // when
        final List<GameTimeline> found = gameTimelineRepository
            .findAllByGameSessionIdOrderByTurnNumberAsc(gameSession.getGameSessionId());

        // then
        assertThat(found)
            .extracting(GameTimeline::getTurnNumber, GameTimeline::getCash, GameTimeline::getTotalAssets)
            .containsExactly(
                tuple(1, 13_000_000, 18_000_000),
                tuple(2, 14_500_000, 20_000_000)
            );
    }

    @DisplayName("같은 세션의 같은 턴 타임라인은 중복 저장할 수 없다")
    @Test
    void saveDuplicateTurnTimeline() {
        // given
        final GameSession gameSession = saveGameSession(32L, 2);
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            gameSession.getGameSessionId(),
            4,
            LocalDate.of(2026, 6, 1),
            19_000_000,
            21_000_000,
            24_000_000,
            5_000_000,
            3_000_000,
            3_800_000
        ));

        final GameTimeline duplicate = GameTimeline.create(
            gameSession.getGameSessionId(),
            4,
            LocalDate.of(2026, 6, 1),
            20_000_000,
            22_000_000,
            25_000_000,
            5_500_000,
            3_000_000,
            3_900_000
        );

        // when & then
        assertThatThrownBy(() -> gameTimelineRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private GameSession saveGameSession(
        final Long userId,
        final Integer slotNumber
    ) {
        return gameSessionRepository.saveAndFlush(GameSession.create(
            userId,
            slotNumber,
            "타임라인 세션",
            CharacterType.FEMALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "24",
            "24110",
            303L,
            DataSourceType.MY_DATA
        ));
    }
}
