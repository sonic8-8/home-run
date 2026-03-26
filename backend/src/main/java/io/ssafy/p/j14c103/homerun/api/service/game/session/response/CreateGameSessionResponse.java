package io.ssafy.p.j14c103.homerun.api.service.game.session.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateGameSessionResponse {

    private Long sessionId;
    private Integer slotNumber;
    private SessionStatus sessionStatus;
    private Integer currentTurn;
    private DataSourceType dataSourceType;

    @Builder
    private CreateGameSessionResponse(
        final Long sessionId,
        final Integer slotNumber,
        final SessionStatus sessionStatus,
        final Integer currentTurn,
        final DataSourceType dataSourceType
    ) {
        this.sessionId = sessionId;
        this.slotNumber = slotNumber;
        this.sessionStatus = sessionStatus;
        this.currentTurn = currentTurn;
        this.dataSourceType = dataSourceType;
    }

    public static CreateGameSessionResponse of(
        final Long sessionId,
        final Integer slotNumber,
        final SessionStatus sessionStatus,
        final Integer currentTurn,
        final DataSourceType dataSourceType
    ) {
        return CreateGameSessionResponse.builder()
            .sessionId(sessionId)
            .slotNumber(slotNumber)
            .sessionStatus(sessionStatus)
            .currentTurn(currentTurn)
            .dataSourceType(dataSourceType)
            .build();
    }

    public static CreateGameSessionResponse from(final GameSession gameSession) {
        return CreateGameSessionResponse.of(
            gameSession.getGameSessionId(),
            gameSession.getSlotNumber(),
            gameSession.getSessionStatus(),
            gameSession.getCurrentTurn(),
            gameSession.getDataSourceType()
        );
    }
}
