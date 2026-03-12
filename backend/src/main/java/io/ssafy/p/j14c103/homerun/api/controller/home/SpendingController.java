package io.ssafy.p.j14c103.homerun.api.controller.home;

import io.ssafy.p.j14c103.homerun.api.dto.home.SpendingResponse;
import io.ssafy.p.j14c103.homerun.api.service.home.SpendingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class SpendingController {

    private final SpendingService spendingService;

    public SpendingController(final SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @GetMapping("/spending")
    public ResponseEntity<SpendingResponse> getSpending(
            @RequestParam final String userKey,
            @RequestParam(required = false) final String month) {
        final SpendingResponse response = spendingService.getSpending(userKey, month);
        return ResponseEntity.ok(response);
    }
}
