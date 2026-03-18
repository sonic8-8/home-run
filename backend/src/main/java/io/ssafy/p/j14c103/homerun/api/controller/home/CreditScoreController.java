package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.CreditScoreService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    @GetMapping("/score")
    public ApiResponse<CreditScoreResponse> getCreditScore(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final CreditScoreResponse response = creditScoreService.getCreditScore(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }
}
