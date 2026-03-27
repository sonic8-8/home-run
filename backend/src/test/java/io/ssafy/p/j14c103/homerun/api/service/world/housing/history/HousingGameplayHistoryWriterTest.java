package io.ssafy.p.j14c103.homerun.api.service.world.housing.history;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
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
class HousingGameplayHistoryWriterTest {

    @Autowired
    private HousingGameplayHistoryWriter housingGameplayHistoryWriter;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @DisplayName("주거 변경 이력을 기록하면 before와 after 상태가 gameplay history에 저장된다")
    @Test
    void writeHousingChange() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        final GameHousing beforeHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );
        final GameHousing afterHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.OWNED_APT,
            Money.of(300_000_000L),
            Money.zero(),
            Money.of(150_000L),
            301L
        );

        // when
        final GameplayHistory history = housingGameplayHistoryWriter.writeHousingChange(
            beforeHousing,
            afterHousing,
            12,
            "자가 아파트를 마련했다."
        );

        // then
        assertThat(history.getTableName()).isEqualTo("게임주거");
        assertThat(history.getColumnName()).isEqualTo("주거형태,보증금,월세,관리비,현재매물ID");
        assertThat(history.getBeforeValue()).contains("\"housingType\":\"STUDIO\"");
        assertThat(history.getAfterValue()).contains("\"housingType\":\"OWNED_APT\"");
        assertThat(history.getAfterValue()).contains("\"propertyId\":301");

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdAndTableNameOrderByOccurredTurnAscHistoryIdAsc(
                Math.toIntExact(gameSession.getGameSessionId()),
                "게임주거"
            );
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getSummary()).isEqualTo("자가 아파트를 마련했다.");
        assertThat(histories.get(0).getOccurredTurn()).isEqualTo(12);
    }

    private GameSession createGameSession() {
        return GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            101L,
            DataSourceType.PROFILE
        );
    }
}
