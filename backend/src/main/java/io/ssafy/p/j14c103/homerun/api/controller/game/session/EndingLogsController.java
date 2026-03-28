package io.ssafy.p.j14c103.homerun.api.controller.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.EndingLogsService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameTimelineResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/logs")
@RequiredArgsConstructor
public class EndingLogsController {

    private final EndingLogsService endingLogsService;

    @GetMapping
    public ApiResponse<GameTimelineResponse> getLogs(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @PathVariable final Long sessionId
    ) {
        final GameTimelineResponse response = endingLogsService.getLogs(
            authenticatedUser.getUserId(),
            sessionId
        );
        return ApiResponse.ok(response);
    }
}
