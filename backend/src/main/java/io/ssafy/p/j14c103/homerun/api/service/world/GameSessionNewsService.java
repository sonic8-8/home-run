package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameSessionNewsResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameSessionNewsService {

    private final GameSessionRepository gameSessionRepository;
    private final GameNewsLogRepository gameNewsLogRepository;
    private final NewsMasterRepository newsMasterRepository;
    private final LatestTurnNewsPhaseService latestTurnNewsPhaseService;
    private final GameWorldRollService gameWorldRollService;

    public GameSessionNewsResult getCurrentTurnNews(final Long gameSessionId) {
        final GameSession gameSession = findGameSession(gameSessionId);
        final Integer currentTurn = requireCurrentTurn(gameSession);
        final LocalDate currentDate = requireCurrentDate(gameSession);

        final GameNewsLog savedNewsLog = findSavedNewsLog(gameSessionId, currentTurn);
        if (savedNewsLog != null) {
            final NewsMaster savedNews = findNewsMaster(savedNewsLog.getNewsId());
            return createResult(gameSession, savedNews, savedNewsLog.getPublishedDate());
        }

        final int worldRoll = gameWorldRollService.resolveTurnRoll(gameSessionId, currentTurn);
        final LatestTurnNewsPhaseService.PhaseResolution phaseResolution =
            latestTurnNewsPhaseService.resolve(gameSession.getCyclePhase(), worldRoll);
        final NewsMaster selectedNews = selectNews(
            gameSessionId,
            phaseResolution.getEconomicCycleType(),
            worldRoll
        );

        gameNewsLogRepository.save(
            GameNewsLog.create(
                gameSessionId,
                currentTurn,
                selectedNews.getNewsId(),
                selectedNews.getTitle(),
                currentDate
            )
        );

        return createResult(gameSession, selectedNews, currentDate);
    }

    private GameSession findGameSession(final Long gameSessionId) {
        return gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
    }

    private Integer requireCurrentTurn(final GameSession gameSession) {
        if (gameSession.getCurrentTurn() != null) {
            return gameSession.getCurrentTurn();
        }

        throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
    }

    private LocalDate requireCurrentDate(final GameSession gameSession) {
        if (gameSession.getCurrentDate() != null) {
            return gameSession.getCurrentDate();
        }

        throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
    }

    private GameNewsLog findSavedNewsLog(final Long gameSessionId, final Integer currentTurn) {
        final List<GameNewsLog> newsLogs = gameNewsLogRepository
            .findAllByGameSessionIdAndTurnNumberOrderByGameNewsLogIdAsc(
                gameSessionId,
                currentTurn
            );
        if (newsLogs.isEmpty()) {
            return null;
        }

        return newsLogs.get(0);
    }

    private NewsMaster selectNews(
        final Long gameSessionId,
        final String economicCycleType,
        final int worldRoll
    ) {
        final List<NewsMaster> candidates = newsMasterRepository
            .findAllByEconomicCycleTypeOrderByNewsIdAsc(economicCycleType);
        if (candidates.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        final int selectedIndex = gameWorldRollService.selectIndex(
            gameSessionId,
            worldRoll,
            economicCycleType,
            candidates.size()
        );
        return candidates.get(selectedIndex);
    }

    private NewsMaster findNewsMaster(final String newsId) {
        return newsMasterRepository.findById(newsId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));
    }

    private GameSessionNewsResult createResult(
        final GameSession gameSession,
        final NewsMaster newsMaster,
        final LocalDate publishedDate
    ) {
        return GameSessionNewsResult.of(
            gameSession.getCurrentTurn(),
            gameSession.getCurrentDate(),
            List.of(
                GameSessionNewsResult.NewsItem.of(
                    newsMaster.getNewsId(),
                    newsMaster.getTitle(),
                    newsMaster.getArticleText(),
                    newsMaster.getSourceName(),
                    publishedDate,
                    newsMaster.getEconomicCycleType()
                )
            )
        );
    }
}
