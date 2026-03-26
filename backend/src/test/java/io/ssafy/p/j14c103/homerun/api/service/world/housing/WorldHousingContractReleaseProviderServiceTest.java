package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.HousingContractReleaseProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldHousingContractReleaseProviderServiceTest {

    @Autowired
    private WorldHousingContractReleaseProviderService worldHousingContractReleaseProviderService;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @AfterEach
    void tearDown() {
        gameHousingRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
    }

    @DisplayName("현재 주거가 원룸이면 퇴거 기준 데이터로 보증금 환급과 NONE 전환 값을 반환한다")
    @Test
    void getReleaseCriteriaForStudioHousing() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.STUDIO));
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        ));

        // when
        final HousingContractReleaseProviderResponse response =
            worldHousingContractReleaseProviderService.getReleaseCriteria(gameSession.getGameSessionId());

        // then
        assertThat(response.getPreviousHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(response.getNextHousingType()).isEqualTo(HousingType.NONE);
        assertThat(response.getRefundedDeposit()).isEqualTo(Money.of(10_000_000L));
        assertThat(response.getPreviousPropertyId()).isEqualTo(201L);
    }

    @DisplayName("현재 주거가 자가 아파트면 환급 보증금은 0으로 반환한다")
    @Test
    void getReleaseCriteriaForOwnedAptHousing() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.OWNED_APT));
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.OWNED_APT,
            Money.of(900_000_000L),
            Money.zero(),
            Money.of(250_000L),
            301L
        ));

        // when
        final HousingContractReleaseProviderResponse response =
            worldHousingContractReleaseProviderService.getReleaseCriteria(gameSession.getGameSessionId());

        // then
        assertThat(response.getPreviousHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(response.getNextHousingType()).isEqualTo(HousingType.NONE);
        assertThat(response.getRefundedDeposit()).isEqualTo(Money.zero());
        assertThat(response.getPreviousPropertyId()).isEqualTo(301L);
    }

    @DisplayName("현재 주거 계약이 없으면 주거 계약 없음 예외를 던진다")
    @Test
    void getReleaseCriteriaWithoutCurrentContract() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.NONE));

        // when
        // then
        assertThatThrownBy(() -> worldHousingContractReleaseProviderService.getReleaseCriteria(
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_CURRENT_CONTRACT_NOT_FOUND);
    }

    @DisplayName("현재 주거 타입이 NONE이면 주거 계약 없음 예외를 던진다")
    @Test
    void getReleaseCriteriaWithNoneHousingState() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.NONE));
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.NONE,
            Money.zero(),
            Money.zero(),
            Money.zero(),
            null
        ));

        // when
        // then
        assertThatThrownBy(() -> worldHousingContractReleaseProviderService.getReleaseCriteria(
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_CURRENT_CONTRACT_NOT_FOUND);
    }

    @DisplayName("임대형 주거인데 보증금이 없으면 월드 결과 예외를 던진다")
    @Test
    void getReleaseCriteriaWithRentalHousingWithoutDeposit() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.VILLA));
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.VILLA,
            null,
            Money.of(400_000L),
            Money.of(70_000L),
            401L
        ));

        // when
        // then
        assertThatThrownBy(() -> worldHousingContractReleaseProviderService.getReleaseCriteria(
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_RESULT_INVALID);
    }

    @DisplayName("자가 아파트인데 현재 매물 ID가 없으면 월드 결과 예외를 던진다")
    @Test
    void getReleaseCriteriaWithOwnedAptWithoutPropertyId() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(HousingType.OWNED_APT));
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.OWNED_APT,
            Money.of(1_200_000_000L),
            Money.zero(),
            Money.of(300_000L),
            null
        ));

        // when
        // then
        assertThatThrownBy(() -> worldHousingContractReleaseProviderService.getReleaseCriteria(
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_RESULT_INVALID);
    }

    private GameSession createGameSession(final HousingType housingType) {
        return GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            housingType,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
    }
}
