package io.ssafy.p.j14c103.homerun.api.service.world.ending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorldEndingHistoryProviderServiceTest {

    @Autowired
    private WorldEndingHistoryProviderService worldEndingHistoryProviderService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("엔딩 히스토리는 뉴스와 이벤트를 turn 오름차순으로 집계하고 현재 주거 스냅샷을 함께 반환한다")
    @Test
    void getEndingHistory() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(450L));
        gameNewsLogRepository.saveAndFlush(
            GameNewsLog.create(
                gameSession.getGameSessionId(),
                10,
                "NEWS-002",
                "채용 한파 심화",
                LocalDate.of(2026, 10, 1)
            )
        );
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
                9,
                2002,
                11,
                "B",
                java.util.Map.of("stress", -2),
                "전화를 끊고 피해를 막았다.",
                LocalDateTime.of(2026, 9, 18, 10, 30)
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
                .beforeValue("{\"housingType\":\"STUDIO\",\"propertyId\":201}")
                .afterValue("{\"housingType\":\"VILLA\",\"propertyId\":202}")
                .summary("전세 빌라로 이사했다.")
                .occurredTurn(6)
                .build()
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

        // when
        final WorldEndingHistoryProviderResponse response =
            worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId());

        // then
        assertThat(response.getNewsHistories())
            .extracting(WorldEndingHistoryProviderResponse.NewsHistoryItem::getTurnNumber)
            .containsExactly(8, 10);
        assertThat(response.getEventHistories())
            .extracting(WorldEndingHistoryProviderResponse.EventHistoryItem::getTurnNumber)
            .containsExactly(7, 9);
        assertThat(response.getHousingHistories())
            .extracting(WorldEndingHistoryProviderResponse.HousingHistoryItem::getTurnNumber)
            .containsExactly(6, 12);
        assertThat(response.getHousingHistories().get(0).getAfterState().getHousingType())
            .isEqualTo(HousingType.VILLA);
        assertThat(response.getHousingHistories().get(1).getAfterState().getPropertyId())
            .isEqualTo(301L);
        assertThat(response.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(response.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(301L);
        assertThat(response.getHousingSnapshot().getTargetPropertyId()).isEqualTo(450L);
    }

    @DisplayName("뉴스와 이벤트 로그가 없어도 빈 배열과 기본 주거 스냅샷을 반환한다")
    @Test
    void getEndingHistoryWithoutLogs() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(777L));

        // when
        final WorldEndingHistoryProviderResponse response =
            worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId());

        // then
        assertThat(response.getNewsHistories()).isEmpty();
        assertThat(response.getEventHistories()).isEmpty();
        assertThat(response.getHousingHistories()).isEmpty();
        assertThat(response.getHousingSnapshot().getCurrentHousingType()).isNull();
        assertThat(response.getHousingSnapshot().getCurrentPropertyId()).isNull();
        assertThat(response.getHousingSnapshot().getTargetPropertyId()).isEqualTo(777L);
    }

    @DisplayName("존재하지 않는 세션이면 WORLD_SESSION_NOT_FOUND 예외가 발생한다")
    @Test
    void getEndingHistoryWithUnknownSession() {
        // when & then
        assertThatThrownBy(() -> worldEndingHistoryProviderService.getEndingHistory(9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    private GameSession createGameSession(final Long targetPropertyId) {
        return GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            targetPropertyId,
            DataSourceType.PROFILE
        );
    }
}
