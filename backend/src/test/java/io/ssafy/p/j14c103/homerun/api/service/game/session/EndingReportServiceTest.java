package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.EndingReportResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportAchievement;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class EndingReportServiceTest {

    @Autowired
    private EndingReportService endingReportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("엔딩 리포트 조회는 요약과 world histories를 함께 반환한다")
    @Test
    void getEndingReport() {
        final User user = userRepository.save(
            User.register(Email.of("ending-user@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createEndedSession(user.getId(), 450L)
        );
        gameReportRepository.saveAndFlush(GameReport.create(
            gameSession.getGameSessionId(),
            SessionStatus.CLEAR,
            "부동산 갑부",
            120_000_000,
            80_000_000,
            "S",
            500_000_000,
            40_000_000,
            "식비",
            BigDecimal.valueOf(35.2),
            List.of(
                GameReportAchievement.of("첫 내집 마련", "/images/badges/first-house.png")
            )
        ));
        gameNewsLogRepository.saveAndFlush(
            GameNewsLog.create(
                gameSession.getGameSessionId(),
                8,
                "NEWS-001",
                "금리 인하 기조 지속",
                LocalDate.of(2026, 8, 1)
            )
        );
        gameEventLogRepository.saveAndFlush(
            GameEventLog.create(
                gameSession.getGameSessionId(),
                7,
                2001,
                10,
                "A",
                java.util.Map.of("cash", 100000),
                "지원금을 신청했다.",
                LocalDateTime.of(2026, 7, 18, 10, 30)
            )
        );
        gameHousingRepository.saveAndFlush(
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.OWNED_APT,
                Money.of(300_000_000L),
                Money.zero(),
                Money.of(150_000L),
                301L
            )
        );
        gameplayHistoryRepository.saveAndFlush(
            GameplayHistory.builder()
                .gameId(Math.toIntExact(gameSession.getGameSessionId()))
                .eventId(920001)
                .tableName("게임주거")
                .columnName("주거형태,보증금,월세,관리비,현재매물ID")
                .targetKey1(String.valueOf(gameSession.getGameSessionId()))
                .beforeValue("{\"housingType\":\"VILLA\",\"propertyId\":202}")
                .afterValue("{\"housingType\":\"OWNED_APT\",\"propertyId\":301}")
                .summary("자가 아파트를 마련했다.")
                .occurredTurn(12)
                .build()
        );

        final EndingReportResponse response = endingReportService.getEndingReport(
            user.getId(),
            gameSession.getGameSessionId()
        );

        assertThat(response.getEndingType()).isEqualTo(SessionStatus.CLEAR);
        assertThat(response.getTitle()).isEqualTo("부동산 갑부");
        assertThat(response.getTotalAssets()).isEqualTo(500_000_000L);
        assertThat(response.getSpendingPattern().getTopCategory()).isEqualTo("식비");
        assertThat(response.getAchievements())
            .extracting(EndingReportResponse.AchievementResponse::getName)
            .containsExactly("첫 내집 마련");
        assertThat(response.getNewsHistories())
            .extracting(EndingReportResponse.NewsHistoryResponse::getNewsId)
            .containsExactly("NEWS-001");
        assertThat(response.getEventHistories())
            .extracting(EndingReportResponse.EventHistoryResponse::getGameEventId)
            .containsExactly(2001);
        assertThat(response.getHousingHistories())
            .extracting(EndingReportResponse.HousingHistoryResponse::getTurnNumber)
            .containsExactly(12);
        assertThat(response.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(301L);
        assertThat(response.getHousingSnapshot().getTargetPropertyId()).isEqualTo(450L);
    }

    @DisplayName("엔딩 리포트가 아직 없으면 ENDING_REPORT_NOT_READY가 발생한다")
    @Test
    void getEndingReportWithoutReport() {
        final User user = userRepository.save(
            User.register(Email.of("ending-not-ready@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 777L)
        );

        assertThatThrownBy(() -> endingReportService.getEndingReport(
                user.getId(),
                gameSession.getGameSessionId()
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.ENDING_REPORT_NOT_READY);
    }

    @DisplayName("진행 중인 세션이면 엔딩 리포트를 조회할 수 없다")
    @Test
    void getEndingReportWhenSessionIsInProgress() {
        final User user = userRepository.save(
            User.register(Email.of("ending-in-progress@example.com"), "tester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 777L)
        );
        gameReportRepository.saveAndFlush(createGameReport(gameSession.getGameSessionId()));

        assertThatThrownBy(() -> endingReportService.getEndingReport(
                user.getId(),
                gameSession.getGameSessionId()
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.ENDING_REPORT_NOT_READY);
    }

    @DisplayName("다른 사용자의 세션이면 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void getEndingReportForbidden() {
        final User owner = userRepository.save(
            User.register(Email.of("ending-owner@example.com"), "owner", "hashed")
        );
        final User requester = userRepository.save(
            User.register(Email.of("ending-requester@example.com"), "requester", "hashed")
        );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 900L)
        );
        gameReportRepository.saveAndFlush(GameReport.create(
            gameSession.getGameSessionId(),
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
        ));

        assertThatThrownBy(() -> endingReportService.getEndingReport(
                requester.getId(),
                gameSession.getGameSessionId()
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
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

    private GameSession createEndedSession(final Long userId, final Long targetPropertyId) {
        final GameSession gameSession = createGameSession(userId, targetPropertyId);
        gameSession.markEnding(SessionStatus.CLEAR);
        return gameSession;
    }

    private GameSession createGameSession(final Long userId, final Long targetPropertyId) {
        return GameSession.create(
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
    }
}
