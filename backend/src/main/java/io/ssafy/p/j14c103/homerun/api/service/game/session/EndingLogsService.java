package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameTimelineResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EndingLogsService {

    private final GameSessionRepository gameSessionRepository;
    private final GameTimelineRepository gameTimelineRepository;
    private final GameReportRepository gameReportRepository;
    private final UserAuthContextService userAuthContextService;

    public GameTimelineResponse getLogs(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);

        if (!gameReportRepository.existsById(sessionId)) {
            throw new HomerunException(ErrorCode.ENDING_REPORT_NOT_READY);
        }

        return GameTimelineResponse.from(
            gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAscGameTimelineIdAsc(sessionId)
        );
    }
}
