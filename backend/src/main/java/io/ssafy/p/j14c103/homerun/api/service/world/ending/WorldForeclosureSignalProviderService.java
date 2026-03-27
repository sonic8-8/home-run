package io.ssafy.p.j14c103.homerun.api.service.world.ending;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.request.WorldForeclosureSignalProviderRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldForeclosureSignalProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldForeclosureSignalProviderService {

    private static final int LONG_OVERDUE_THRESHOLD = 3;

    private final GameSessionRepository gameSessionRepository;

    public WorldForeclosureSignalProviderResponse getForeclosureSignal(
        final WorldForeclosureSignalProviderRequest request
    ) {
        validateRequest(request);
        gameSessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        final boolean signalGenerated = request.getLongOverdueTurns() >= LONG_OVERDUE_THRESHOLD
            && request.isForcedSaleOccurred()
            && request.isHousingLossSignal();

        return WorldForeclosureSignalProviderResponse.of(
            request.getSessionId(),
            signalGenerated,
            request.getLongOverdueTurns(),
            request.isForcedSaleOccurred(),
            request.isHousingLossSignal()
        );
    }

    private void validateRequest(final WorldForeclosureSignalProviderRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
