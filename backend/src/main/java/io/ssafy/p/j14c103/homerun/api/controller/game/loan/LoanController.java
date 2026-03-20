package io.ssafy.p.j14c103.homerun.api.controller.game.loan;

import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanApplyRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanCalculateRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanConfirmRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.loan.request.LoanRepayRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanProductService;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanService;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanApplyResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanCalculateResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanConfirmResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanRepayResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final LoanProductService loanProductService;

    /**
     * 대출 상품 목록 조회.
     * GET /api/games/sessions/{sessionId}/loans/products
     */
    @GetMapping("/products")
    public ApiResponse<List<LoanProductResponse>> getProducts(
            @PathVariable final Integer sessionId,
            @RequestParam(defaultValue = "ALL") final String category,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final List<LoanProductResponse> products = loanProductService.getProducts(category, page, size);
        return ApiResponse.ok(products);
    }

    /**
     * 대출 상품 상세 조회.
     * GET /api/games/sessions/{sessionId}/loans/products/{productId}
     */
    @GetMapping("/products/{productId}")
    public ApiResponse<LoanProductDetailResponse> getProductDetail(
            @PathVariable final Integer sessionId,
            @PathVariable final String productId
    ) {
        final LoanProductDetailResponse detail = loanProductService.getProductDetail(productId)
                .orElseThrow(() -> new HomerunException(ErrorCode.LOAN_PRODUCT_NOT_FOUND));
        return ApiResponse.ok(detail);
    }

    /**
     * 이자 계산기.
     * POST /api/games/sessions/{sessionId}/loans/calculate
     */
    @PostMapping("/calculate")
    public ApiResponse<LoanCalculateResponse> calculate(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanCalculateRequest request
    ) {
        final LoanCalculateResponse response = loanService.calculate(
                request.principal(), request.annualRate(),
                request.termMonths(), request.repaymentMethod());
        return ApiResponse.ok(response);
    }

    /**
     * 대출 심사 신청.
     * POST /api/games/sessions/{sessionId}/loans/apply
     */
    @PostMapping("/apply")
    public ApiResponse<LoanApplyResponse> apply(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanApplyRequest request
    ) {
        final LoanApplyResponse response = loanService.apply(
                sessionId, request.productId(), request.propertyId());
        return ApiResponse.ok(response);
    }

    /**
     * 대출 최종 확정.
     * POST /api/games/sessions/{sessionId}/loans/confirm
     */
    @PostMapping("/confirm")
    public ApiResponse<LoanConfirmResponse> confirm(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanConfirmRequest request
    ) {
        final LoanConfirmResponse response = loanService.confirm(
                sessionId, request.applicationId(),
                request.requestedAmount(), request.agreed());
        return ApiResponse.ok(response);
    }

    /**
     * 중도 일시 상환.
     * POST /api/games/sessions/{sessionId}/loans/repay
     */
    @PostMapping("/repay")
    public ApiResponse<LoanRepayResponse> repay(
            @PathVariable final Integer sessionId,
            @RequestBody final LoanRepayRequest request
    ) {
        final LoanRepayResponse response = loanService.repay(
                sessionId, request.loanId(), request.amount());
        return ApiResponse.ok(response);
    }
}
