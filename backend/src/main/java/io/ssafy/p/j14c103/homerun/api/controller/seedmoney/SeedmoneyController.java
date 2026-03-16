package io.ssafy.p.j14c103.homerun.api.controller.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyCreateRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seedmoney")
@RequiredArgsConstructor
public class SeedmoneyController {

    private final SeedmoneyService seedmoneyService;

    @PostMapping("/create")
    public ResponseEntity<SeedmoneyAccountResponse> createAccount(
            @Valid @RequestBody final SeedmoneyCreateRequest request) {
        final SeedmoneyAccountResponse response = seedmoneyService.createAccount(
                request.getUserId(), request.getUserKey(), request.getAccountTypeUniqueNo());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/account")
    public ResponseEntity<SeedmoneyAccountResponse> getAccount(
            @RequestParam final Long userId,
            @RequestParam final String userKey) {
        final SeedmoneyAccountResponse response = seedmoneyService.getAccount(userId, userKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<SeedmoneyTransactionResponse> transfer(
            @Valid @RequestBody final SeedmoneyTransferRequest request) {
        final SeedmoneyTransactionResponse response = seedmoneyService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<SeedmoneyTransactionResponse> deposit(
            @Valid @RequestBody final SeedmoneyDepositRequest request) {
        final SeedmoneyTransactionResponse response = seedmoneyService.deposit(request);
        return ResponseEntity.ok(response);
    }
}
