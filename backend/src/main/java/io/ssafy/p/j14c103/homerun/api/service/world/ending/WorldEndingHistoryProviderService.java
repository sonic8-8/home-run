package io.ssafy.p.j14c103.homerun.api.service.world.ending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
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

    private static final String GAME_HOUSING_TABLE_NAME = "게임주거";

    private final GameSessionRepository gameSessionRepository;
    private final GameNewsLogRepository gameNewsLogRepository;
    private final GameEventLogRepository gameEventLogRepository;
    private final GameHousingRepository gameHousingRepository;
    private final GameplayHistoryRepository gameplayHistoryRepository;
    private final ObjectMapper objectMapper;

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
            gameplayHistoryRepository.findAllByGameIdAndTableNameOrderByOccurredTurnAscHistoryIdAsc(
                    Math.toIntExact(gameSessionId),
                    GAME_HOUSING_TABLE_NAME
                )
                .stream()
                .map(this::toHousingHistoryItem)
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

    private WorldEndingHistoryProviderResponse.HousingHistoryItem toHousingHistoryItem(
        final GameplayHistory gameplayHistory
    ) {
        return WorldEndingHistoryProviderResponse.HousingHistoryItem.of(
            gameplayHistory.getOccurredTurn(),
            gameplayHistory.getSummary(),
            toHousingStateItem(gameplayHistory.getBeforeValue()),
            toHousingStateItem(gameplayHistory.getAfterValue())
        );
    }

    private WorldEndingHistoryProviderResponse.HousingStateItem toHousingStateItem(
        final String json
    ) {
        if (json == null || json.isBlank()) {
            return WorldEndingHistoryProviderResponse.HousingStateItem.of(null, null);
        }

        try {
            final JsonNode node = objectMapper.readTree(json);
            return WorldEndingHistoryProviderResponse.HousingStateItem.of(
                resolveHousingType(node.get("housingType")),
                resolveLong(node.get("propertyId"))
            );
        } catch (JsonProcessingException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR, exception);
        }
    }

    private HousingType resolveHousingType(final JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return HousingType.valueOf(node.asText());
    }

    private Long resolveLong(final JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }
}
