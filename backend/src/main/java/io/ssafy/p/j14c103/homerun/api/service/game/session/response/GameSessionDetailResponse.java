package io.ssafy.p.j14c103.homerun.api.service.game.session.response;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameSessionDetailResponse {

    private Long sessionId;
    private Integer slotNumber;
    private String characterName;
    private CharacterType characterType;
    private JobType jobType;
    private HousingType housingType;
    private String regionCode;
    private String districtCode;
    private Long targetPropertyId;
    private DataSourceType dataSourceType;
    private Integer currentTurn;
    private LocalDate currentDate;
    private CyclePhase cyclePhase;
    private Long cashBalance;
    private Long netWorth;
    private SessionStatus sessionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime lastPlayedAt;

    @Builder
    private GameSessionDetailResponse(
        final Long sessionId,
        final Integer slotNumber,
        final String characterName,
        final CharacterType characterType,
        final JobType jobType,
        final HousingType housingType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final DataSourceType dataSourceType,
        final Integer currentTurn,
        final LocalDate currentDate,
        final CyclePhase cyclePhase,
        final Long cashBalance,
        final Long netWorth,
        final SessionStatus sessionStatus,
        final LocalDateTime createdAt,
        final LocalDateTime lastPlayedAt
    ) {
        this.sessionId = sessionId;
        this.slotNumber = slotNumber;
        this.characterName = characterName;
        this.characterType = characterType;
        this.jobType = jobType;
        this.housingType = housingType;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.targetPropertyId = targetPropertyId;
        this.dataSourceType = dataSourceType;
        this.currentTurn = currentTurn;
        this.currentDate = currentDate;
        this.cyclePhase = cyclePhase;
        this.cashBalance = cashBalance;
        this.netWorth = netWorth;
        this.sessionStatus = sessionStatus;
        this.createdAt = createdAt;
        this.lastPlayedAt = lastPlayedAt;
    }

    public static GameSessionDetailResponse of(
        final Long sessionId,
        final Integer slotNumber,
        final String characterName,
        final CharacterType characterType,
        final JobType jobType,
        final HousingType housingType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final DataSourceType dataSourceType,
        final Integer currentTurn,
        final LocalDate currentDate,
        final CyclePhase cyclePhase,
        final Long cashBalance,
        final Long netWorth,
        final SessionStatus sessionStatus,
        final LocalDateTime createdAt,
        final LocalDateTime lastPlayedAt
    ) {
        return GameSessionDetailResponse.builder()
            .sessionId(sessionId)
            .slotNumber(slotNumber)
            .characterName(characterName)
            .characterType(characterType)
            .jobType(jobType)
            .housingType(housingType)
            .regionCode(regionCode)
            .districtCode(districtCode)
            .targetPropertyId(targetPropertyId)
            .dataSourceType(dataSourceType)
            .currentTurn(currentTurn)
            .currentDate(currentDate)
            .cyclePhase(cyclePhase)
            .cashBalance(cashBalance)
            .netWorth(netWorth)
            .sessionStatus(sessionStatus)
            .createdAt(createdAt)
            .lastPlayedAt(lastPlayedAt)
            .build();
    }

    public static GameSessionDetailResponse from(final GameSession gameSession) {
        return GameSessionDetailResponse.of(
            gameSession.getGameSessionId(),
            gameSession.getSlotNumber(),
            gameSession.getCharacterName(),
            gameSession.getCharacterType(),
            gameSession.getJobType(),
            gameSession.getHousingType(),
            gameSession.getRegionCode(),
            gameSession.getDistrictCode(),
            gameSession.getTargetPropertyId(),
            gameSession.getDataSourceType(),
            gameSession.getCurrentTurn(),
            gameSession.getCurrentDate(),
            gameSession.getCyclePhase(),
            gameSession.getCashBalance().getAmount().longValue(),
            gameSession.getNetWorth().getAmount().longValue(),
            gameSession.getSessionStatus(),
            gameSession.getCreatedAt(),
            gameSession.getLastPlayedAt()
        );
    }
}
