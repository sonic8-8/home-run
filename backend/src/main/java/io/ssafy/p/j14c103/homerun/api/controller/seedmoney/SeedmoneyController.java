package io.ssafy.p.j14c103.homerun.api.controller.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seedmoney")
@RequiredArgsConstructor
public class SeedmoneyController {

    private final SeedmoneyService seedmoneyService;

    @GetMapping("/account")
    public ResponseEntity<SeedmoneyAccountResponse> getAccount(
            @RequestParam final Long userId,
            @RequestParam final String userKey) {
        final SeedmoneyAccountResponse response = seedmoneyService.getAccount(userId, userKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody final SeedmoneyTransferRequest request) {
        seedmoneyService.transfer(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@Valid @RequestBody final SeedmoneyDepositRequest request) {
        seedmoneyService.deposit(request);
        return ResponseEntity.ok().build();
    }
}
