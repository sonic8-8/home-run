package io.ssafy.p.j14c103.homerun.domain.gamesession.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class GameHousingRepositoryTest {

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @DisplayName("GameHousing을 저장하면 현재 주거 상태를 다시 조회할 수 있다.")
    @Test
    void saveGameHousing() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        final GameHousing gameHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );

        // when
        final GameHousing saved = gameHousingRepository.save(gameHousing);

        // then
        assertThat(saved.getGameSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(saved.getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(saved.getCurrentDeposit()).isEqualTo(Money.of(10_000_000L));
        assertThat(saved.getMonthlyRent()).isEqualTo(Money.of(500_000L));
        assertThat(saved.getMaintenanceFee()).isEqualTo(Money.of(80_000L));
        assertThat(saved.getCurrentPropertyId()).isEqualTo(201L);
    }

    @DisplayName("같은 gameSessionId로 다시 저장하면 현재 주거 상태가 갱신된다.")
    @Test
    void updateCurrentHousingByGameSessionId() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        final GameHousing first = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );

        final GameHousing updated = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.VILLA,
            Money.of(20_000_000L),
            Money.zero(),
            Money.of(120_000L),
            202L
        );

        gameHousingRepository.saveAndFlush(first);

        // when
        gameHousingRepository.saveAndFlush(updated);

        // then
        assertThat(gameHousingRepository.count()).isEqualTo(1);
        assertThat(gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId())).isPresent();
        assertThat(gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId()).orElseThrow()
            .getCurrentHousingType()).isEqualTo(HousingType.VILLA);
        assertThat(gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId()).orElseThrow()
            .getCurrentPropertyId()).isEqualTo(202L);
    }

    @DisplayName("gameSessionId로 현재 GameHousing을 조회할 수 있다.")
    @Test
    void findByGameSessionId() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        final GameHousing gameHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );

        gameHousingRepository.save(gameHousing);

        // when
        final Optional<GameHousing> result = gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId());

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getGameSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(result.orElseThrow().getCurrentPropertyId()).isEqualTo(201L);
        assertThat(result.orElseThrow().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
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
