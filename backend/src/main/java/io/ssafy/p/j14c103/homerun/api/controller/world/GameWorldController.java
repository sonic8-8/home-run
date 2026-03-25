package io.ssafy.p.j14c103.homerun.api.controller.world;

import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldService;
import io.ssafy.p.j14c103.homerun.api.service.world.LatestTurnNewsService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games/sessions")
public class GameWorldController {

    private final GameWorldService gameWorldService;
    private final LatestTurnNewsService latestTurnNewsService;

    @GetMapping("/{gameSessionId}/turn")
    public ApiResponse<GameTurnResponse> getTurn(
        @PathVariable final Long gameSessionId
    ) {
        final GameTurnResponse response = gameWorldService.getTurn(gameSessionId);

        return ApiResponse.ok(response);
    }

    @GetMapping("/{gameSessionId}/news/latest")
    public ApiResponse<LatestTurnNewsResponse> getLatestTurnNews(
        @PathVariable final Long gameSessionId
    ) {
        final LatestTurnNewsResponse response = latestTurnNewsService.getLatestTurnNews(
            gameSessionId
        );

        return ApiResponse.ok(response);
    }
}
