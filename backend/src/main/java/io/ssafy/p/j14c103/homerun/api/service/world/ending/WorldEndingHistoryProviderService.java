package io.ssafy.p.j14c103.homerun.api.service.world.ending;

import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldEndingHistoryProviderService {

    private final GameSessionRepository gameSessionRepository;
    private final GameNewsLogRepository gameNewsLogRepository;
    private final GameEventLogRepository gameEventLogRepository;
    private final GameHousingRepository gameHousingRepository;

    public WorldEndingHistoryProviderResponse getEndingHistory(final Long gameSessionId) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        return WorldEndingHistoryProviderResponse.of(
            gameNewsLogRepository.findAllByGameSessionIdOrderByTurnNumberAscGameNewsLogIdAsc(gameSessionId)
                .stream()
                .map(this::toNewsHistoryItem)
                .toList(),
            gameEventLogRepository.findAllByGameSessionIdOrderByTurnNumberAscGameEventLogIdAsc(gameSessionId)
                .stream()
                .map(this::toEventHistoryItem)
                .toList(),
            gameHousingRepository.findByGameSessionId(gameSessionId)
                .map(gameHousing -> WorldEndingHistoryProviderResponse.HousingSnapshotItem.of(
                    gameHousing.getCurrentHousingType(),
                    gameHousing.getCurrentPropertyId(),
                    gameSession.getTargetPropertyId()
                ))
                .orElseGet(() -> WorldEndingHistoryProviderResponse.HousingSnapshotItem.of(
                    null,
                    null,
                    gameSession.getTargetPropertyId()
                ))
        );
    }

    private WorldEndingHistoryProviderResponse.NewsHistoryItem toNewsHistoryItem(
        final GameNewsLog gameNewsLog
    ) {
        return WorldEndingHistoryProviderResponse.NewsHistoryItem.of(
            gameNewsLog.getTurnNumber(),
            gameNewsLog.getNewsId(),
            gameNewsLog.getHeadlineSnapshot(),
            gameNewsLog.getPublishedDate()
        );
    }

    private WorldEndingHistoryProviderResponse.EventHistoryItem toEventHistoryItem(
        final GameEventLog gameEventLog
    ) {
        return WorldEndingHistoryProviderResponse.EventHistoryItem.of(
            gameEventLog.getTurnNumber(),
            gameEventLog.getGameEventId(),
            gameEventLog.getSelectedChoiceCode(),
            gameEventLog.getResultSummary(),
            gameEventLog.getResolvedAt()
        );
    }
}
