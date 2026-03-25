package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameSessionNewsResult;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LatestTurnNewsService {

    private final GameSessionNewsService gameSessionNewsService;

    public LatestTurnNewsResponse getLatestTurnNews(final Long gameSessionId) {
        final GameSessionNewsResult result = gameSessionNewsService.getCurrentTurnNews(gameSessionId);
        return LatestTurnNewsResponse.from(result);
    }
}
