package io.ssafy.p.j14c103.homerun.api.service.world.ending.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class WorldForeclosureSignalProviderResponse {

    private final Long sessionId;
    private final boolean signalGenerated;
    private final int longOverdueTurns;
    private final boolean forcedSaleOccurred;
    private final boolean housingLossSignal;

    private WorldForeclosureSignalProviderResponse(
        final Long sessionId,
        final boolean signalGenerated,
        final int longOverdueTurns,
        final boolean forcedSaleOccurred,
        final boolean housingLossSignal
    ) {
        validateSessionId(sessionId);
        validateLongOverdueTurns(longOverdueTurns);

        this.sessionId = sessionId;
        this.signalGenerated = signalGenerated;
        this.longOverdueTurns = longOverdueTurns;
        this.forcedSaleOccurred = forcedSaleOccurred;
        this.housingLossSignal = housingLossSignal;
    }

    public static WorldForeclosureSignalProviderResponse of(
        final Long sessionId,
        final boolean signalGenerated,
        final int longOverdueTurns,
        final boolean forcedSaleOccurred,
        final boolean housingLossSignal
    ) {
        return new WorldForeclosureSignalProviderResponse(
            sessionId,
            signalGenerated,
            longOverdueTurns,
            forcedSaleOccurred,
            housingLossSignal
        );
    }

    private void validateSessionId(final Long sessionId) {
        if (sessionId == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateLongOverdueTurns(final int longOverdueTurns) {
        if (longOverdueTurns < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
