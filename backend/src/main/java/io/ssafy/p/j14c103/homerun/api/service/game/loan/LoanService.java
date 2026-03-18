package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanApprovalService.ApprovalResult;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanCalculator.CalculationResult;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanApplyResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanCalculateResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanConfirmResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanRepayResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplication;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplicationRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 대출 서비스.
 * 싸피론 + FSS 대출 상품 + 심사 + 확정 + 상환 전체를 관리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final GameLoanRepository gameLoanRepository;
    private final LoanApprovalService loanApprovalService;

    /**
     * 이자 계산기.
     */
    public LoanCalculateResponse calculate(
            final int principal,
            final double annualRate,
            final int termMonths,
            final String repaymentMethod
    ) {
        final RepaymentType type = RepaymentType.valueOf(repaymentMethod);
        final CalculationResult result = LoanCalculator.calculate(principal, annualRate, termMonths, type);
        return LoanCalculateResponse.from(result);
    }

    /**
     * 대출 심사 신청.
     */
    @Transactional
    public LoanApplyResponse apply(
            final Integer sessionId,
            final String productId,
            final Integer propertyId,
            final int annualSalary,
            final String jobType,
            final int cssGrade,
            final String regionCode,
            final Integer propertyPrice
    ) {
        final LoanType loanType = determineLoanType(productId, propertyId);

        final ApprovalResult approvalResult = loanApprovalService.evaluate(
                loanType, annualSalary, jobType, cssGrade, regionCode, propertyPrice, sessionId);

        final LoanApplication application = LoanApplication.create(
                sessionId, loanType, productId, propertyId);

        if (approvalResult.approved()) {
            application.approve(approvalResult.maxLoanAmount());
        } else {
            application.reject(approvalResult.rejectionReason());
        }

        loanApplicationRepository.save(application);

        log.info("대출 심사 완료: sessionId={}, type={}, approved={}, limit={}",
                sessionId, loanType, approvalResult.approved(), approvalResult.maxLoanAmount());

        return LoanApplyResponse.from(application);
    }

    /**
     * 대출 최종 확정. APPROVED → GameLoan 생성.
     *
     * @return 확정된 대출 정보 + 세션에 입금할 금액
     */
    @Transactional
    public LoanConfirmResponse confirm(
            final Integer sessionId,
            final Integer applicationId,
            final int requestedAmount,
            final boolean agreed
    ) {
        if (!agreed) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new HomerunException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));

        if (!application.isApproved()) {
            throw new HomerunException(ErrorCode.LOAN_NOT_APPROVED);
        }

        if (requestedAmount > application.getApprovedLimitAmount()) {
            throw new HomerunException(ErrorCode.LOAN_EXCEED_LIMIT);
        }

        application.confirm();

        // 기본 360턴(30년) 상환, 원리금균등
        final int termMonths = 360;
        final double annualRate = 3.49; // TODO: 상품별 금리 조회
        final CalculationResult calcResult = LoanCalculator.calculate(
                requestedAmount, annualRate, termMonths, RepaymentType.EQUAL_PRINCIPAL_INTEREST);

        final GameLoan gameLoan = GameLoan.create(
                sessionId,
                application.getProductId(),
                requestedAmount,
                BigDecimal.valueOf(annualRate),
                calcResult.monthlyPayment(),
                termMonths,
                application.getProductId(),
                RepaymentType.EQUAL_PRINCIPAL_INTEREST
        );

        gameLoanRepository.save(gameLoan);

        log.info("대출 확정: sessionId={}, loanId={}, amount={}", sessionId, gameLoan.getGameLoanId(), requestedAmount);

        return LoanConfirmResponse.from(gameLoan);
    }

    /**
     * 싸피론 대출 신청 (세션당 1건).
     *
     * @return 생성된 GameLoan + 세션에 입금할 금액
     */
    @Transactional
    public GameLoan applySsafyLoan(final Integer sessionId, final int principal) {
        final int existing = gameLoanRepository.countByGameSessionIdAndProductIdAndLoanStatus(
                sessionId, "SSAFY_LOAN", LoanStatus.ACTIVE);
        if (existing > 0) {
            throw new HomerunException(ErrorCode.LOAN_SSAFY_DUPLICATE);
        }

        final GameLoan loan = GameLoan.createSsafyLoan(sessionId, principal);
        gameLoanRepository.save(loan);

        log.info("싸피론 대출 실행: sessionId={}, amount={}", sessionId, principal);
        return loan;
    }

    /**
     * 중도 상환.
     */
    @Transactional
    public LoanRepayResponse repay(final Integer sessionId, final Integer loanId, final int amount) {
        final GameLoan loan = gameLoanRepository.findById(loanId)
                .orElseThrow(() -> new HomerunException(ErrorCode.INVALID_INPUT_VALUE));

        if (!loan.isActive()) {
            throw new HomerunException(ErrorCode.LOAN_ALREADY_REPAID);
        }

        loan.repay(amount);

        log.info("대출 상환: loanId={}, repaid={}, remaining={}", loanId, amount, loan.getPrincipalAmount());

        return LoanRepayResponse.from(loan, amount);
    }

    /**
     * 세션의 활성 대출 목록.
     */
    public List<GameLoan> getActiveLoans(final Integer sessionId) {
        return gameLoanRepository.findAllByGameSessionIdAndLoanStatus(sessionId, LoanStatus.ACTIVE);
    }

    /**
     * 정산 시 모든 활성 대출의 월 이자 합계 차감.
     *
     * @return 총 이자 차감액
     */
    @Transactional
    public int settleMonthlyInterest(final Integer sessionId) {
        final List<GameLoan> activeLoans = getActiveLoans(sessionId);
        return activeLoans.stream()
                .mapToInt(GameLoan::chargeMonthlyInterest)
                .sum();
    }

    private LoanType determineLoanType(final String productId, final Integer propertyId) {
        if (propertyId == null) {
            return LoanType.CREDIT;
        }
        // TODO: productId 기반으로 JEONSE/MORTGAGE 구분 로직 필요
        return LoanType.MORTGAGE;
    }
}
