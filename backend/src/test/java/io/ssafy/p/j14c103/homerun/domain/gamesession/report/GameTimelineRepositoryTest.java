package io.ssafy.p.j14c103.homerun.domain.gamesession.report;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private UserRepository userRepository;

    @DisplayName("game_timelines는 turn asc, id asc 순으로 조회된다")
    @Test
    void findAllByGameSessionIdOrderByTurnNumberAscGameTimelineIdAsc() {
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
            12,
            LocalDate.of(2026, 12, 1),
            5_000_000,
            25_000_000,
            75_000_000,
            5_000_000,
            50_000_000,
            2_200_000
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
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            gameSession.getGameSessionId(),
            12,
            LocalDate.of(2026, 12, 15),
            6_000_000,
            26_000_000,
            76_000_000,
            6_000_000,
            49_000_000,
            2_200_000
        ));

        assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAscGameTimelineIdAsc(
                gameSession.getGameSessionId()
            ))
            .extracting(GameTimeline::getTurnNumber, GameTimeline::getLoggedDate)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, LocalDate.of(2026, 1, 1)),
                org.assertj.core.groups.Tuple.tuple(12, LocalDate.of(2026, 12, 1)),
                org.assertj.core.groups.Tuple.tuple(12, LocalDate.of(2026, 12, 15))
            );
    }
}
