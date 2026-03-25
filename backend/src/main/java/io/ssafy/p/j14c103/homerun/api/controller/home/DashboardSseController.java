package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardSseService;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class DashboardSseController {

    private final DashboardSseService dashboardSseService;

    @GetMapping(value = "/dashboard/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        return dashboardSseService.subscribe(authenticatedUser.getUserId());
    }
}
