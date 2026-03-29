package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GameTimelineRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private GameTimelineRepository gameTimelineRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("game_timelines는 턴 종료 스냅샷을 turn 오름차순으로 조회한다")
    @Test
    void findAllByGameSessionIdOrderByTurnNumberAsc() {
        final User user = userRepository.save(
            User.register(Email.of("game-timeline@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(GameSession.create(
            user.getId(),
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            450L,
            DataSourceType.PROFILE
        ));
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
            LocalDate.of(2026, 1, 1),
            13_000_000,
            13_000_000,
            13_000_000,
            0,
            0,
            2_000_000
        ));

        assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
                gameSession.getGameSessionId()
            ))
            .extracting(GameTimeline::getTurnNumber, GameTimeline::getCash, GameTimeline::getTotalAssets)
            .containsExactly(
                tuple(1, 13_000_000, 13_000_000),
                tuple(2, 14_500_000, 20_000_000)
            );
    }

    @DisplayName("같은 세션의 같은 턴 타임라인은 중복 저장할 수 없다")
    @Test
    void saveDuplicateTurnTimeline() {
        final User user = userRepository.save(
            User.register(Email.of("game-timeline-duplicate@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(GameSession.create(
            user.getId(),
            2,
            "타임라인 세션",
            CharacterType.FEMALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "24",
            "24110",
            303L,
            DataSourceType.MY_DATA
        ));
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

        assertThatThrownBy(() -> gameTimelineRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
