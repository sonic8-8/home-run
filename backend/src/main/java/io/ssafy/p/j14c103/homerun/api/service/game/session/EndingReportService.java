package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.EndingReportResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.WorldEndingHistoryProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EndingReportService {

    private final GameSessionRepository gameSessionRepository;
    private final GameReportRepository gameReportRepository;
    private final WorldEndingHistoryProviderService worldEndingHistoryProviderService;
    private final UserAuthContextService userAuthContextService;

    public EndingReportResponse getEndingReport(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);

        final GameReport gameReport = gameReportRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.ENDING_REPORT_NOT_READY));
        final WorldEndingHistoryProviderResponse history =
            worldEndingHistoryProviderService.getEndingHistory(sessionId);

        return EndingReportResponse.of(gameSession.getCharacterType(), gameReport, history);
    }
}
