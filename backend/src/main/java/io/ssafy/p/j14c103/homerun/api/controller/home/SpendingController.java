package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.SpendingService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingService spendingService;

    @GetMapping("/spending")
    public ApiResponse<SpendingResponse> getSpending(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) final String month) {
        final SpendingResponse response = spendingService.getSpending(authenticatedUser.getUserId(), month);
        return ApiResponse.ok(response);
    }
}
