package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetGameSessionListService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;

    public GameSessionListResponse getSessions(final Long userId) {
        userAuthContextService.getContext(userId);
        final List<GameSession> gameSessions = gameSessionRepository.findAllByUserIdOrderBySlotNumberAsc(userId);
        return GameSessionListResponse.from(gameSessions);
    }
}
