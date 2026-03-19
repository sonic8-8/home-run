package io.ssafy.p.j14c103.homerun.api.controller.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyCreateRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyTransactionResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seedmoney")
@RequiredArgsConstructor
public class SeedmoneyController {

    private final SeedmoneyService seedmoneyService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SeedmoneyAccountResponse> createAccount(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final SeedmoneyCreateRequest request) {
        final SeedmoneyAccountResponse response = seedmoneyService.createAccount(
                authenticatedUser.getUserId(),
                request.toServiceRequest());
        return ApiResponse.created(response);
    }

    @GetMapping("/account")
    public ApiResponse<SeedmoneyAccountResponse> getAccount(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser) {
        final SeedmoneyAccountResponse response = seedmoneyService.getAccount(authenticatedUser.getUserId());
        return ApiResponse.ok(response);
    }

    @PostMapping("/transfer")
    public ApiResponse<SeedmoneyTransactionResponse> transfer(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final SeedmoneyTransferRequest request) {
        final SeedmoneyTransactionResponse response = seedmoneyService.transfer(
                authenticatedUser.getUserId(),
                request.toServiceRequest());
        return ApiResponse.ok(response);
    }

    @PostMapping("/deposit")
    public ApiResponse<SeedmoneyTransactionResponse> deposit(
            @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
            @Valid @RequestBody final SeedmoneyDepositRequest request) {
        final SeedmoneyTransactionResponse response = seedmoneyService.deposit(
                authenticatedUser.getUserId(),
                request.toServiceRequest());
        return ApiResponse.ok(response);
    }
}
