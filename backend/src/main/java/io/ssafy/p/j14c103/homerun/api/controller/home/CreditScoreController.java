package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.service.home.CreditScoreService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    @GetMapping("/score")
    public ResponseEntity<CreditScoreResponse> getCreditScore(@RequestParam final String userKey) {
        final CreditScoreResponse response = creditScoreService.getCreditScore(userKey);
        return ResponseEntity.ok(response);
    }
}
