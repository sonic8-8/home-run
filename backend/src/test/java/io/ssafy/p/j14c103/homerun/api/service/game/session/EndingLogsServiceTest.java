package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameTimelineResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimeline;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EndingLogsServiceTest {

    @Autowired
    private EndingLogsService endingLogsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private GameTimelineRepository gameTimelineRepository;

    @DisplayName("엔딩 로그 조회는 turn 오름차순 timeline을 반환한다")
    @Test
    void getLogs() {
        final User user = userRepository.save(
            User.register(Email.of("ending-logs@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createEndedSession(user.getId(), 450L)
        );
        gameReportRepository.saveAndFlush(createGameReport(gameSession.getGameSessionId()));
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

        final GameTimelineResponse response = endingLogsService.getLogs(
            user.getId(),
            gameSession.getGameSessionId()
        );

        assertThat(response.getTimeline())
            .extracting(GameTimelineResponse.TimelineItemResponse::getTurnNumber)
            .containsExactly(1, 12);
        assertThat(response.getTimeline().get(0).getCash()).isEqualTo(13_000_000L);
        assertThat(response.getTimeline().get(1).getLoanBalance()).isEqualTo(50_000_000L);
    }

    @DisplayName("엔딩 리포트가 아직 없으면 ENDING_REPORT_NOT_READY가 발생한다")
    @Test
    void getLogsWithoutReport() {
        final User user = userRepository.save(
            User.register(Email.of("ending-logs-not-ready@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createEndedSession(user.getId(), 777L)
        );

        assertThatThrownBy(() -> endingLogsService.getLogs(user.getId(), gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.ENDING_REPORT_NOT_READY);
    }

    @DisplayName("다른 사용자의 세션이면 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void getLogsForbidden() {
        final User owner = userRepository.save(
            User.register(Email.of("ending-logs-owner@example.com"), "owner", "hashed")
        );
        final User requester = userRepository.save(
            User.register(Email.of("ending-logs-requester@example.com"), "requester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createEndedSession(owner.getId(), 900L)
        );
        gameReportRepository.saveAndFlush(createGameReport(gameSession.getGameSessionId()));

        assertThatThrownBy(() -> endingLogsService.getLogs(requester.getId(), gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    private GameSession createEndedSession(final Long userId, final Long targetPropertyId) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
        gameSession.markEnding(SessionStatus.CLEAR);
        return gameSession;
    }

    private GameReport createGameReport(final Long gameSessionId) {
        return GameReport.create(
            gameSessionId,
            SessionStatus.CLEAR,
            "부동산 갑부",
            120_000_000,
            80_000_000,
            "S",
            500_000_000,
            40_000_000,
            "식비",
            BigDecimal.valueOf(35.2),
            List.of()
        );
    }
}
