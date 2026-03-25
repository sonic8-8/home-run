package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanApprovalService.ApprovalResult;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanCalculator.CalculationResult;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanApplyServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanCalculateServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanConfirmServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanRepayServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanApplyResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanCalculateResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanConfirmResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanRepayResponse;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplication;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplicationRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 대출 서비스.
 * 싸피론 + 금감원 대출 상품 + 심사 + 확정 + 상환 전체를 관리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private static final int MAX_INT_VALUE = Integer.MAX_VALUE;

    private final LoanApplicationRepository loanApplicationRepository;
    private final GameLoanRepository gameLoanRepository;
    private final LoanApprovalService loanApprovalService;
    private final LoanProductService loanProductService;
    private final GameSessionRepository gameSessionRepository;
    private final GameCareerRepository gameCareerRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final CreditScoreProvider creditScoreProvider;

    public LoanCalculateResponse calculate(final LoanCalculateServiceRequest request) {
        return calculate(
            request.getPrincipal(),
            request.getAnnualRate(),
            request.getTermMonths(),
            request.getRepaymentMethod()
        );
    }

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

    @Transactional
    public LoanApplyResponse apply(final Long sessionId, final LoanApplyServiceRequest request) {
        return apply(sessionId, request.getProductId(), request.getPropertyId());
    }

    @Transactional
    public LoanApplyResponse apply(
            final Long sessionId,
            final String productId,
            final Long propertyId
    ) {
        final LoanSessionData sessionData = loadLoanSessionData(sessionId, propertyId);
        final LoanType loanType = determineLoanType(productId, propertyId);

        final ApprovalResult approvalResult = loanApprovalService.evaluate(
                loanType, sessionData.annualSalary(), sessionData.jobType(),
                sessionData.cssGrade(), sessionData.regionCode(),
                sessionData.propertyPrice(), sessionId);

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
    public LoanConfirmResponse confirm(final Long sessionId, final LoanConfirmServiceRequest request) {
        return confirm(
            sessionId,
            request.getApplicationId(),
            request.getRequestedAmount(),
            request.isAgreed()
        );
    }

    @Transactional
    public LoanConfirmResponse confirm(
            final Long sessionId,
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

        final int termMonths = 360;
        final double annualRate = loanProductService.getProductRate(application.getProductId());
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

        log.info("대출 확정: sessionId={}, loanId={}, amount={}, rate={}",
                sessionId, gameLoan.getGameLoanId(), requestedAmount, annualRate);

        return LoanConfirmResponse.from(gameLoan);
    }

    /**
     * 싸피론 대출 신청 (세션당 1건).
     *
     * @return 생성된 GameLoan + 세션에 입금할 금액
     */
    @Transactional
    public GameLoan applySsafyLoan(final Long sessionId, final int principal) {
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
    public LoanRepayResponse repay(final Long sessionId, final LoanRepayServiceRequest request) {
        return repay(sessionId, request.getLoanId(), request.getAmount());
    }

    @Transactional
    public LoanRepayResponse repay(final Long sessionId, final Integer loanId, final int amount) {
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
    public List<GameLoan> getActiveLoans(final Long sessionId) {
        return gameLoanRepository.findAllByGameSessionIdAndLoanStatus(sessionId, LoanStatus.ACTIVE);
    }

    /**
     * 정산 시 모든 활성 대출의 월 이자 합계 차감.
     *
     * @return 총 이자 차감액
     */
    @Transactional
    public int settleMonthlyInterest(final Long sessionId) {
        final List<GameLoan> activeLoans = getActiveLoans(sessionId);
        return activeLoans.stream()
                .mapToInt(GameLoan::chargeMonthlyInterest)
                .sum();
    }

    /**
     * 대출 유형 결정.
     * 금감원 상품 유형 조회 또는 propertyId 기반.
     */
    private LoanType determineLoanType(final String productId, final Long propertyId) {
        if (propertyId == null) {
            return LoanType.CREDIT;
        }

        final String productType = loanProductService.getProductType(productId);
        if (productType != null) {
            if (productType.contains("전세")) {
                return LoanType.JEONSE;
            }
            if (productType.contains("주택") || productType.contains("담보")) {
                return LoanType.MORTGAGE;
            }
        }

        return LoanType.MORTGAGE;
    }

    private LoanSessionData loadLoanSessionData(final Long sessionId, final Long propertyId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final GameCareer gameCareer = gameCareerRepository.findById(convertSessionId(sessionId))
            .orElseThrow(() -> new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID));

        final int annualSalary = requirePositiveSalary(gameCareer);
        final int cssGrade = creditScoreProvider.calculate(gameSession.getUserId()).getGrade();
        final Integer propertyPrice = resolvePropertyPrice(gameSession, propertyId);

        return new LoanSessionData(
            annualSalary,
            gameSession.getJobType().name(),
            cssGrade,
            gameSession.getRegionCode(),
            propertyPrice
        );
    }

    private Integer resolvePropertyPrice(final GameSession gameSession, final Long propertyId) {
        final Long targetPropertyId = propertyId != null ? propertyId : gameSession.getTargetPropertyId();
        if (targetPropertyId == null) {
            return null;
        }

        final RealEstateProperty property = realEstatePropertyRepository.findById(targetPropertyId)
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        return property.getBasePrice()
            .getAmount()
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact();
    }

    private int requirePositiveSalary(final GameCareer gameCareer) {
        final Integer salary = gameCareer.getSalary();
        if (salary == null || salary <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return salary;
    }

    private Integer convertSessionId(final Long sessionId) {
        if (sessionId == null || sessionId <= 0 || sessionId > MAX_INT_VALUE) {
            throw new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID);
        }

        return Math.toIntExact(sessionId);
    }

    private record LoanSessionData(
        int annualSalary,
        String jobType,
        int cssGrade,
        String regionCode,
        Integer propertyPrice
    ) {
    }
}
