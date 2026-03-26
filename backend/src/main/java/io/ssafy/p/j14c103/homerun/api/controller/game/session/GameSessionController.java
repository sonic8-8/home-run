package io.ssafy.p.j14c103.homerun.api.controller.game.session;

import io.ssafy.p.j14c103.homerun.api.controller.game.session.request.CreateGameSessionRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.CreateGameSessionService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.DeleteGameSessionService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.GetGameSessionDetailService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.GetGameSessionListService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions")
@RequiredArgsConstructor
public class GameSessionController {

    private final GetGameSessionListService getGameSessionListService;
    private final CreateGameSessionService createGameSessionService;
    private final GetGameSessionDetailService getGameSessionDetailService;
    private final DeleteGameSessionService deleteGameSessionService;

    @GetMapping
    public ApiResponse<GameSessionListResponse> getSessions(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser
    ) {
        final GameSessionListResponse response =
            getGameSessionListService.getSessions(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateGameSessionResponse> createSession(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @Valid @RequestBody final CreateGameSessionRequest request
    ) {
        final CreateGameSessionResponse response = createGameSessionService.create(
            authenticatedUser.getUserId(),
            request.toServiceRequest()
        );
        return ApiResponse.created(response);
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<GameSessionDetailResponse> getSessionDetail(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final GameSessionDetailResponse response =
            getGameSessionDetailService.getSessionDetail(authenticatedUser.getUserId(), sessionId);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        deleteGameSessionService.delete(authenticatedUser.getUserId(), sessionId);
        return ApiResponse.ok(null);
    }
}
