package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> getDashboard(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final DashboardResponse response = dashboardService.getDashboard(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }
}
