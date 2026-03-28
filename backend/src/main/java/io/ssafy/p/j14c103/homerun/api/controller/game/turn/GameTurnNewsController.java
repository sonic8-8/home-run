package io.ssafy.p.j14c103.homerun.api.controller.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnNewsService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/news/latest")
@RequiredArgsConstructor
public class GameTurnNewsController {

    private final GameTurnNewsService gameTurnNewsService;

    @GetMapping
    public ApiResponse<LatestTurnNewsResponse> getLatestTurnNews(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final LatestTurnNewsResponse response = gameTurnNewsService.getLatestTurnNews(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }
}
