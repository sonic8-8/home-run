package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class GameHousingRepositoryTest {

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @DisplayName("GameHousing을 저장하면 목표 매물 스냅샷과 현재 주거 상태를 다시 조회할 수 있다.")
    @Test
    void saveGameHousing() {
        // given
        GameHousing gameHousing = GameHousing.create(
                1L,
                101L,
                "SEOUL",
                Money.of(450_000_000L),
                HousingType.STUDIO,
                Money.of(10_000_000L),
                Money.of(500_000L),
                Money.of(80_000L)
        );

        // when
        GameHousing saved = gameHousingRepository.save(gameHousing);

        // then
        assertThat(saved.getSessionId()).isEqualTo(1L);
        assertThat(saved.getTargetPropertyId()).isEqualTo(101L);
        assertThat(saved.getTargetRegion()).isEqualTo("SEOUL");
        assertThat(saved.getTargetHousePrice()).isEqualTo(Money.of(450_000_000L));
        assertThat(saved.getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(saved.getMaintenanceFee()).isEqualTo(Money.of(80_000L));
    }

    @DisplayName("한 세션에는 GameHousing을 하나만 저장할 수 있다.")
    @Test
    void saveOnlyOneGameHousingPerSession() {
        // given
        GameHousing first = GameHousing.create(
                1L,
                101L,
                "SEOUL",
                Money.of(450_000_000L),
                HousingType.STUDIO,
                Money.of(10_000_000L),
                Money.of(500_000L),
                Money.of(80_000L)
        );

        GameHousing duplicate = GameHousing.create(
                1L,
                202L,
                "GWANGJU",
                Money.of(320_000_000L),
                HousingType.VILLA,
                Money.of(20_000_000L),
                Money.of(0L),
                Money.of(120_000L)
        );

        gameHousingRepository.saveAndFlush(first);

        // when // then
        assertThatThrownBy(() -> gameHousingRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("sessionId로 현재 GameHousing을 조회할 수 있다.")
    @Test
    void findBySessionId() {
        // given
        GameHousing gameHousing = GameHousing.create(
                1L,
                101L,
                "SEOUL",
                Money.of(450_000_000L),
                HousingType.STUDIO,
                Money.of(10_000_000L),
                Money.of(500_000L),
                Money.of(80_000L)
        );

        gameHousingRepository.save(gameHousing);

        // when
        Optional<GameHousing> result = gameHousingRepository.findBySessionId(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.orElseThrow().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
    }
}
