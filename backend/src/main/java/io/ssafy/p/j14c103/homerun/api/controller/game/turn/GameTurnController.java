package io.ssafy.p.j14c103.homerun.api.controller.game.turn;

import io.ssafy.p.j14c103.homerun.api.controller.game.turn.request.SubmitTurnSlotsRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnActionService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnStateService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.SubmitTurnSlotsService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.AvailableActionsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnPreviewResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/turn")
@RequiredArgsConstructor
public class GameTurnController {

    private final GameTurnStateService gameTurnStateService;
    private final GameTurnActionService gameTurnActionService;
    private final SubmitTurnSlotsService submitTurnSlotsService;

    @GetMapping
    public ApiResponse<TurnStateResponse> getTurnState(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final TurnStateResponse response = gameTurnStateService.getTurnState(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/actions")
    public ApiResponse<AvailableActionsResponse> getAvailableActions(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final AvailableActionsResponse response = gameTurnActionService.getAvailableActions(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/slots")
    public ApiResponse<TurnPreviewResponse> submitTurnSlots(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @Valid @RequestBody final SubmitTurnSlotsRequest request
    ) {
        final TurnPreviewResponse response = submitTurnSlotsService.submitTurnSlots(
            authenticatedUser.getUserId(),
            sessionId,
            request.toServiceRequest()
        );
        return ApiResponse.ok(response);
    }
}
