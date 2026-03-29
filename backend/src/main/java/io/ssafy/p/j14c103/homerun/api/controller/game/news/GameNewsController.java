package io.ssafy.p.j14c103.homerun.api.controller.game.news;

import io.ssafy.p.j14c103.homerun.api.service.game.news.GameNewsHistoryService;
import io.ssafy.p.j14c103.homerun.api.service.game.news.response.GameNewsHistoryResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/news")
@RequiredArgsConstructor
public class GameNewsController {

    private final GameNewsHistoryService gameNewsHistoryService;

    @GetMapping("/history")
    public ApiResponse<GameNewsHistoryResponse> getNewsHistory(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final GameNewsHistoryResponse response = gameNewsHistoryService.getNewsHistory(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }
}
