package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GameWorldServiceTest {

    @Autowired
    private GameWorldService gameWorldService;

    @Autowired
    private GameSessionRefRepository gameSessionRefRepository;

    @AfterEach
    void tearDown() {
        gameSessionRefRepository.deleteAllInBatch();
    }

    @DisplayName("세션 스냅샷을 읽어 턴 조회 응답으로 조립한다")
    @Test
    void getTurn() {
        // given
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "BOOM"));

        // when
        GameTurnResponse response = gameWorldService.getTurn(1001);

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getEconomicCycle().getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getEconomicCycle().getDescription()).isEqualTo("경기 호황기");
        assertThat(response.getNews()).isEmpty();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void getTurnWithUnknownSession() {
        // when & then
        assertThatThrownBy(() -> gameWorldService.getTurn(9999))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션의 경제 사이클 값이 잘못되면 world cycle 상태 에러를 던진다")
    @Test
    void getTurnWithInvalidCycleState() {
        // given
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "INVALID"));

        // when & then
        assertThatThrownBy(() -> gameWorldService.getTurn(1001))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_STATE_INVALID);
    }

    private GameSessionRef createGameSessionRef(int gameSessionId, String economicCycleType) {
        return GameSessionRef.builder()
            .gameId(gameSessionId)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(12)
            .economicCycleType(economicCycleType)
            .currentDate(LocalDate.of(2026, 1, 1))
            .cash(2_000_000)
            .netAssets(2_000_000)
            .inProgress(true)
            .bankrupt(false)
            .cleared(false)
            .createdAt(LocalDateTime.of(2026, 3, 1, 9, 0))
            .lastPlayedAt(LocalDateTime.of(2026, 3, 1, 9, 30))
            .saveSlotId(1)
            .targetRegionCode("SEOUL")
            .targetDistrictCode("GANGNAM")
            .seedType("NORMAL")
            .sessionStatus("IN_PROGRESS")
            .ownedPropertyListingId(0)
            .build();
    }
}
