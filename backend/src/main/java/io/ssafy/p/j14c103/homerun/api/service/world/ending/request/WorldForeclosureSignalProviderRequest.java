package io.ssafy.p.j14c103.homerun.api.service.world.ending.request;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorldForeclosureSignalProviderRequest {

    private Long sessionId;
    private int longOverdueTurns;
    private boolean forcedSaleOccurred;
    private boolean housingLossSignal;

    @Builder(access = AccessLevel.PRIVATE)
    private WorldForeclosureSignalProviderRequest(
        final Long sessionId,
        final int longOverdueTurns,
        final boolean forcedSaleOccurred,
        final boolean housingLossSignal
    ) {
        validateSessionId(sessionId);
        validateLongOverdueTurns(longOverdueTurns);

        this.sessionId = sessionId;
        this.longOverdueTurns = longOverdueTurns;
        this.forcedSaleOccurred = forcedSaleOccurred;
        this.housingLossSignal = housingLossSignal;
    }

    public static WorldForeclosureSignalProviderRequest of(
        final Long sessionId,
        final int longOverdueTurns,
        final boolean forcedSaleOccurred,
        final boolean housingLossSignal
    ) {
        return WorldForeclosureSignalProviderRequest.builder()
            .sessionId(sessionId)
            .longOverdueTurns(longOverdueTurns)
            .forcedSaleOccurred(forcedSaleOccurred)
            .housingLossSignal(housingLossSignal)
            .build();
    }

    private void validateSessionId(final Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateLongOverdueTurns(final int longOverdueTurns) {
        if (longOverdueTurns < 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
