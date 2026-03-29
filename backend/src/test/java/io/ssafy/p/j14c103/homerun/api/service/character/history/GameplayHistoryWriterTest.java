package io.ssafy.p.j14c103.homerun.api.service.character.history;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GameplayHistoryWriterTest extends IntegrationTestSupport {

    @Autowired
    private GameplayHistoryWriter gameplayHistoryWriter;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("스탯 변경을 기록하면 변경 전후 값과 차이 정보가 이력에 저장된다.")
    @Test
    void writeStatChange() {
        // given
        final GameStat gameStat = GameStat.builder()
            .gameId(1001)
            .health(70)
            .fatigue(20)
            .stress(30)
            .happiness(40)
            .knowledge(50)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
        final GameplayHistoryWriter.StatSnapshot beforeStat =
            GameplayHistoryWriter.StatSnapshot.from(gameStat);

        gameStat.applyChange(-10, 5, 7, 3, 8, 2);

        // when
        final GameplayHistory history = gameplayHistoryWriter.writeStatChange(
            beforeStat,
            gameStat,
            2
        );

        // then
        assertThat(history.getHistoryId()).isNotNull();
        assertThat(history.getTableName()).isEqualTo("게임스탯");
        assertThat(history.getColumnName())
            .isEqualTo("체력,피로도,스트레스,행복도,지식,번아웃여부,번아웃시작턴,입원종료턴");
        assertThat(history.getBeforeValue()).contains("\"health\":70");
        assertThat(history.getAfterValue()).contains("\"health\":60");
        assertThat(history.getEffectPayload()).contains("\"knowledgeDelta\":8");
        assertThat(history.getSummary()).isEqualTo("스탯 변경 결과를 반영했습니다.");

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(1001);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getOccurredTurn()).isEqualTo(2);
    }
}
