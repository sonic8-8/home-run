package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class GameHousingRepositoryTest {

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @DisplayName("GameHousing을 저장하면 목표 매물 관련 값들과 현재 주거 상태를 다시 조회할 수 있다.")
    @Test
    void saveGameHousing() {
        // given
        GameHousing gameHousing = GameHousing.create(
            1,
            "SEOUL",
            Money.of(450_000_000L),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L,
            101L
        );

        // when
        GameHousing saved = gameHousingRepository.save(gameHousing);

        // then
        assertThat(saved.getGameSessionId()).isEqualTo(1);
        assertThat(saved.getTargetRegionCode()).isEqualTo("SEOUL");
        assertThat(saved.getTargetHousePrice()).isEqualTo(Money.of(450_000_000L));
        assertThat(saved.getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(saved.getCurrentDeposit()).isEqualTo(Money.of(10_000_000L));
        assertThat(saved.getMonthlyRent()).isEqualTo(Money.of(500_000L));
        assertThat(saved.getMaintenanceFee()).isEqualTo(Money.of(80_000L));
        assertThat(saved.getCurrentPropertyId()).isEqualTo(201L);
        assertThat(saved.getTargetPropertyId()).isEqualTo(101L);
    }

    @DisplayName("같은 gameSessionId로 다시 저장하면 현재 목표매물(주거) 관련 정보들이 갱신된다.")
    @Test
    void updateCurrentHousingByGameSessionId() {
        // given
        GameHousing first = GameHousing.create(
            1,
            "SEOUL",
            Money.of(450_000_000L),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L,
            101L
        );

        GameHousing updated = GameHousing.create(
            1,
            "GWANGJU",
            Money.of(320_000_000L),
            HousingType.VILLA,
            Money.of(20_000_000L),
            Money.of(0L),
            Money.of(120_000L),
            202L,
            102L
        );

        gameHousingRepository.saveAndFlush(first);

        // when
        gameHousingRepository.saveAndFlush(updated);

        // then
        assertThat(gameHousingRepository.count()).isEqualTo(1);
        assertThat(gameHousingRepository.findByGameSessionId(1)).isPresent();
        assertThat(gameHousingRepository.findByGameSessionId(1).orElseThrow().getTargetRegionCode())
            .isEqualTo("GWANGJU");
        assertThat(gameHousingRepository.findByGameSessionId(1).orElseThrow().getCurrentPropertyId())
            .isEqualTo(202L);
    }

    @DisplayName("gameSessionId로 현재 GameHousing을 조회할 수 있다.")
    @Test
    void findByGameSessionId() {
        // given
        GameHousing gameHousing = GameHousing.create(
            1,
            "SEOUL",
            Money.of(450_000_000L),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L,
            101L
        );

        gameHousingRepository.save(gameHousing);

        // when
        Optional<GameHousing> result = gameHousingRepository.findByGameSessionId(1);

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getGameSessionId()).isEqualTo(1);
        assertThat(result.orElseThrow().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.orElseThrow().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
    }
}
