package io.ssafy.p.j14c103.homerun.api.service.game.news;

import io.ssafy.p.j14c103.homerun.api.service.game.news.response.GameNewsHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.WorldEndingHistoryProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameNewsHistoryService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final WorldEndingHistoryProviderService worldEndingHistoryProviderService;

    public GameNewsHistoryResponse getNewsHistory(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final WorldEndingHistoryProviderResponse history =
            worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId());

        return GameNewsHistoryResponse.of(
            history.getNewsHistories().stream()
                .sorted(Comparator.comparing(
                    WorldEndingHistoryProviderResponse.NewsHistoryItem::getTurnNumber
                ).reversed())
                .map(GameNewsHistoryResponse.NewsHistoryResponse::from)
                .toList()
        );
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }
}
