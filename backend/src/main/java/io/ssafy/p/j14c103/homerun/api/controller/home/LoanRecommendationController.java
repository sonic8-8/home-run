package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class LoanRecommendationController {

    private final LoanRecommendationService loanRecommendationService;

    @GetMapping("/loan-recommendations")
    public ResponseEntity<LoanRecommendationResponse> getLoanRecommendations() {
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations();
        return ResponseEntity.ok(response);
    }
}
