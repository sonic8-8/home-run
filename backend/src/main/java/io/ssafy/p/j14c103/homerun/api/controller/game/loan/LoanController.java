package io.ssafy.p.j14c103.homerun.api.controller.game.loan;

import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanApplyRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanCalculateRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanConfirmRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanRepayRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanService;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanApplyResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanCalculateResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanConfirmResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanRepayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게임 대출 컨트롤러.
 * API 스펙 §5.3: /api/games/sessions/{sessionId}/loans/...
 */
@RestController
@RequestMapping("/api/games/sessions/{sessionId}/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * 이자 계산기.
     * POST /api/games/sessions/{sessionId}/loans/calculate
     */
    @PostMapping("/calculate")
    public ResponseEntity<LoanCalculateResponse> calculate(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanCalculateRequest request
    ) {
        final LoanCalculateResponse response = loanService.calculate(
                request.principal(), request.annualRate(),
                request.termMonths(), request.repaymentMethod());
        return ResponseEntity.ok(response);
    }

    /**
     * 대출 심사 신청.
     * POST /api/games/sessions/{sessionId}/loans/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<LoanApplyResponse> apply(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanApplyRequest request
    ) {
        // TODO: GameSession에서 annualSalary, jobType, cssGrade, regionCode, propertyPrice 조회 필요
        final int annualSalary = 36_000_000;
        final String jobType = "LARGE_BIZ";
        final int cssGrade = 2;
        final String regionCode = "SEOUL";
        final Integer propertyPrice = 375_000_000;

        final LoanApplyResponse response = loanService.apply(
                sessionId, request.productId(), request.propertyId(),
                annualSalary, jobType, cssGrade, regionCode, propertyPrice);
        return ResponseEntity.ok(response);
    }

    /**
     * 대출 최종 확정.
     * POST /api/games/sessions/{sessionId}/loans/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<LoanConfirmResponse> confirm(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanConfirmRequest request
    ) {
        final LoanConfirmResponse response = loanService.confirm(
                sessionId, request.applicationId(),
                request.requestedAmount(), request.agreed());
        return ResponseEntity.ok(response);
    }

    /**
     * 중도 일시 상환.
     * POST /api/games/sessions/{sessionId}/loans/repay
     */
    @PostMapping("/repay")
    public ResponseEntity<LoanRepayResponse> repay(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanRepayRequest request
    ) {
        final LoanRepayResponse response = loanService.repay(
                sessionId, request.loanId(), request.amount());
        return ResponseEntity.ok(response);
    }
}
