package io.ssafy.p.j14c103.homerun.api.controller.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.GetTurnStateService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/turn")
@RequiredArgsConstructor
public class GameTurnController {

    private final GetTurnStateService getTurnStateService;

    @GetMapping
    public ApiResponse<TurnStateResponse> getTurnState(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final TurnStateResponse response = getTurnStateService.getTurnState(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }
}
