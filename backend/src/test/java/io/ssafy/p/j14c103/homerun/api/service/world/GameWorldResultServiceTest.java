package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameHousingRepository;
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
class GameWorldResultServiceTest {

    @Autowired
    private GameWorldResultService gameWorldResultService;

    @Autowired
    private GameSessionRefRepository gameSessionRefRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @AfterEach
    void tearDown() {
        gameHousingRepository.deleteAllInBatch();
        gameSessionRefRepository.deleteAllInBatch();
    }

    @DisplayName("턴 커밋용 world result를 누락 필드 없이 조립한다")
    @Test
    void buildWorldResult() {
        // given
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "BOOM"));
        GameHousing gameHousing = GameHousing.create(
            1001,
            "SEOUL",
            Money.of(450_000_000L),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L,
            101L
        );
        gameHousingRepository.saveAndFlush(gameHousing);

        // when
        GameWorldResult result = gameWorldResultService.buildWorldResult(1001, 81);

        // then
        assertThat(result.getCycleResult()).isNotNull();
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(result.getCycleResult().getDescription()).isEqualTo("경기 회복기");
        assertThat(result.getNewsCandidates()).isEmpty();
        assertThat(result.getEventCandidates()).isEmpty();
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(201L);
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isFalse();
    }

    @DisplayName("현재 주거 데이터가 없어도 기본 housing snapshot을 포함한 world result를 반환한다")
    @Test
    void buildWorldResultWithoutHousing() {
        // given
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "RECOVERY"));

        // when
        GameWorldResult result = gameWorldResultService.buildWorldResult(1001, 30);

        // then
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(result.getNewsCandidates()).isEmpty();
        assertThat(result.getEventCandidates()).isEmpty();
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isNull();
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isNull();
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isNull();
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isFalse();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void buildWorldResultWithUnknownSession() {
        // when & then
        assertThatThrownBy(() -> gameWorldResultService.buildWorldResult(9999, 50))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션의 경제 사이클 값이 잘못되면 world cycle 상태 에러를 던진다")
    @Test
    void buildWorldResultWithInvalidCycleState() {
        // given
        gameSessionRefRepository.saveAndFlush(createGameSessionRef(1001, "INVALID"));

        // when & then
        assertThatThrownBy(() -> gameWorldResultService.buildWorldResult(1001, 50))
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
