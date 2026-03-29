package io.ssafy.p.j14c103.homerun.api.controller.game.events;

import io.ssafy.p.j14c103.homerun.api.controller.game.events.request.ResolveEventRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.events.GameEventService;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.PendingEventsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.ResolveEventResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/events")
@RequiredArgsConstructor
public class GameEventController {

    private final GameEventService gameEventService;

    @GetMapping("/pending")
    public ApiResponse<PendingEventsResponse> getPendingEvents(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final PendingEventsResponse response = gameEventService.getPendingEvents(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }

    @PostMapping("/{eventId}/resolve")
    public ApiResponse<ResolveEventResponse> resolveEvent(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId,
        @PathVariable final Integer eventId,
        @RequestBody(required = false) final ResolveEventRequest request
    ) {
        final ResolveEventRequest resolveRequest = request == null ? ResolveEventRequest.empty() : request;
        final ResolveEventResponse response = gameEventService.resolveEvent(
            authenticatedUser.getUserId(),
            sessionId,
            eventId,
            resolveRequest.toServiceRequest()
        );
        return ApiResponse.ok(response);
    }
}
