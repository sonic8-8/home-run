package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
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
public class LoanRecommendationController {

    private final LoanRecommendationService loanRecommendationService;

    @GetMapping("/loan-recommendations")
    public ApiResponse<LoanRecommendationResponse> getLoanRecommendations(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }
}
