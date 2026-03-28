package io.ssafy.p.j14c103.homerun.domain.gamesession.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SettlementLogRepositoryTest {

    @Autowired
    private SettlementLogRepository settlementLogRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("settlement_logs는 현금 변화와 스탯 변화를 저장하고 다시 조회할 수 있다")
    @Test
    void saveSettlementLog() {
        // given
        final GameSession gameSession = saveGameSession(21L, 1);
        settlementLogRepository.saveAndFlush(SettlementLog.create(
            gameSession.getGameSessionId(),
            5,
            SettlementPhaseType.INCOME_EXPENSE,
            "월급과 고정 지출을 반영한다",
            3_200_000,
            Map.of(
                "stress", -4,
                "fatigue", 2
            )
        ));
        entityManager.clear();

        // when
        final List<SettlementLog> found = settlementLogRepository
            .findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(gameSession.getGameSessionId(), 5);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSettlementPhaseType()).isEqualTo(SettlementPhaseType.INCOME_EXPENSE);
        assertThat(found.get(0).getDescription()).isEqualTo("월급과 고정 지출을 반영한다");
        assertThat(found.get(0).getCashChangeAmount()).isEqualTo(3_200_000);
        assertThat(found.get(0).getStatChanges())
            .containsEntry("stress", -4)
            .containsEntry("fatigue", 2);
    }

    private GameSession saveGameSession(
        final Long userId,
        final Integer slotNumber
    ) {
        return gameSessionRepository.saveAndFlush(GameSession.create(
            userId,
            slotNumber,
            "정산 로그 세션",
            CharacterType.MALE,
            JobType.MID_BIZ,
            HousingType.VILLA,
            "11",
            "11710",
            202L,
            DataSourceType.PROFILE
        ));
    }
}
