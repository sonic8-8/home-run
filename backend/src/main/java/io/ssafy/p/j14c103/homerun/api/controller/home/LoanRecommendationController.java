package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class LoanRecommendationController {

    private final LoanRecommendationService loanRecommendationService;

    @GetMapping("/loan-recommendations")
    public ResponseEntity<LoanRecommendationResponse> getLoanRecommendations(
            @RequestParam final Long userId) {
        final LoanRecommendationResponse response = loanRecommendationService.getRecommendations(userId);
        return ResponseEntity.ok(response);
    }
}
