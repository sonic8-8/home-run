package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameWorldServiceTest {

    @Mock
    private GameSessionRefRepository gameSessionRefRepository;

    @Mock
    private CycleTransitionPolicy cycleTransitionPolicy;

    @InjectMocks
    private GameWorldService gameWorldService;

    @DisplayName("세션 스냅샷을 읽어 턴 조회 응답으로 조립한다")
    @Test
    void getTurn() {
        // given
        GameSessionRef gameSessionRef = GameSessionRef.builder()
            .gameId(1001)
            .userId(1)
            .characterName("윤서")
            .characterType(CharacterType.FEMALE)
            .jobTypeSummary(JobType.STARTUP)
            .housingType(HousingType.STUDIO)
            .currentTurn(12)
            .economicCycleType("BOOM")
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

        given(gameSessionRefRepository.findById(1001)).willReturn(Optional.of(gameSessionRef));
        given(cycleTransitionPolicy.descriptionOf(CyclePhase.BOOM)).willReturn("경기 호황기");

        // when
        GameTurnResponse response = gameWorldService.getTurn(1001);

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getEconomicCycle().getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getEconomicCycle().getDescription()).isEqualTo("경기 호황기");
        assertThat(response.getNews()).isEmpty();
    }
}
