package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam final String userKey) {
        final DashboardResponse response = dashboardService.getDashboard(userKey);
        return ResponseEntity.ok(response);
    }
}
