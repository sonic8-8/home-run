package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 대출 심사 서비스.
 * 대출 유형별(개인신용/전세/주택담보) 승인 한도를 계산한다.
 */
@Service
@RequiredArgsConstructor
public class LoanApprovalService {

    private final GameLoanRepository gameLoanRepository;

    /**
     * 대출 심사 결과.
     */
    public record ApprovalResult(boolean approved, int maxLoanAmount, String rejectionReason) {

        public static ApprovalResult approve(final int maxLoanAmount) {
            return new ApprovalResult(true, maxLoanAmount, null);
        }

        public static ApprovalResult reject(final String reason) {
            return new ApprovalResult(false, 0, reason);
        }
    }

    /**
     * 대출 유형별 심사.
     *
     * @param loanType      대출 유형
     * @param annualSalary  연봉
     * @param jobType       직업 유형 (LARGE_BIZ, MID_BIZ, SMALL_BIZ, FREELANCER)
     * @param cssGrade      CSS 등급 (1~5)
     * @param regionCode    지역 코드 (SEOUL, GWANGJU)
     * @param propertyPrice 매물 가격 (전세/주담 전용, nullable)
     * @param sessionId     기존 대출 DSR 계산용
     */
    public ApprovalResult evaluate(
            final LoanType loanType,
            final int annualSalary,
            final String jobType,
            final int cssGrade,
            final String regionCode,
            final Integer propertyPrice,
            final Long sessionId
    ) {
        return switch (loanType) {
            case CREDIT -> evaluateCredit(annualSalary, jobType, cssGrade);
            case JEONSE -> evaluateJeonse(annualSalary, regionCode, propertyPrice, sessionId);
            case MORTGAGE -> evaluateMortgage(annualSalary, regionCode, propertyPrice, sessionId);
        };
    }

    /**
     * 개인신용대출: 한도 = 연봉 × 직업계수 × CSS등급계수
     */
    private ApprovalResult evaluateCredit(
            final int annualSalary, final String jobType, final int cssGrade
    ) {
        final double jobCoefficient = getJobCoefficient(jobType);
        final double cssCoefficient = getCssCoefficient(cssGrade);
        final int maxAmount = (int) (annualSalary * jobCoefficient * cssCoefficient);

        if (maxAmount <= 0) {
            return ApprovalResult.reject("신용등급이 너무 낮아 대출이 불가합니다.");
        }
        return ApprovalResult.approve(maxAmount);
    }

    /**
     * 전세자금대출: 승인한도 = min(LTV한도, DSR한도)
     */
    private ApprovalResult evaluateJeonse(
            final int annualSalary, final String regionCode,
            final Integer propertyPrice, final Long sessionId
    ) {
        if (propertyPrice == null || propertyPrice <= 0) {
            return ApprovalResult.reject("매물 가격 정보가 필요합니다.");
        }

        final double ltvRate = isSeoul(regionCode) ? 0.70 : 0.80;
        final int ltvLimit = (int) (propertyPrice * ltvRate);

        final int dsrLimit = calculateDsrLimit(annualSalary, sessionId);
        if (dsrLimit <= 0) {
            return ApprovalResult.reject("DSR 기준(40%)을 초과하여 대출이 불가합니다.");
        }

        final int maxAmount = Math.min(ltvLimit, dsrLimit);
        return ApprovalResult.approve(maxAmount);
    }

    /**
     * 주택담보대출: 승인한도 = min(LTV한도, DSR한도, DTI한도)
     */
    private ApprovalResult evaluateMortgage(
            final int annualSalary, final String regionCode,
            final Integer propertyPrice, final Long sessionId
    ) {
        if (propertyPrice == null || propertyPrice <= 0) {
            return ApprovalResult.reject("매물 가격 정보가 필요합니다.");
        }

        final double ltvRate = isSeoul(regionCode) ? 0.50 : 0.70;
        final int ltvLimit = (int) (propertyPrice * ltvRate);

        final int dsrLimit = calculateDsrLimit(annualSalary, sessionId);
        if (dsrLimit <= 0) {
            return ApprovalResult.reject("DSR 기준(40%)을 초과하여 대출이 불가합니다.");
        }

        final double dtiRate = isSeoul(regionCode) ? 0.40 : 0.60;
        final int dtiLimit = calculateDtiLimit(annualSalary, dtiRate, sessionId);

        final int maxAmount = Math.min(ltvLimit, Math.min(dsrLimit, dtiLimit));
        if (maxAmount <= 0) {
            return ApprovalResult.reject("DTI/DSR 기준을 초과하여 대출이 불가합니다.");
        }
        return ApprovalResult.approve(maxAmount);
    }

    /**
     * DSR 한도: (연봉 × 0.40 - 기존 대출 연간 상환액) / 12 → 월 상환 가능액 → 원금 환산
     * 간소화: 연봉 × 0.40 - 기존월상환합 × 12 가 양수면 해당 금액을 한도로 사용
     */
    private int calculateDsrLimit(final int annualSalary, final Long sessionId) {
        final int existingAnnualPayment = getExistingAnnualPayment(sessionId);
        final int maxAnnualPayment = (int) (annualSalary * 0.40);
        return maxAnnualPayment - existingAnnualPayment;
    }

    /**
     * DTI 한도: (연봉 × dtiRate - 기타대출 월이자 × 12) 를 한도로 사용
     */
    private int calculateDtiLimit(final int annualSalary, final double dtiRate, final Long sessionId) {
        final int existingAnnualInterest = getExistingAnnualInterest(sessionId);
        return (int) (annualSalary * dtiRate) - existingAnnualInterest;
    }

    private int getExistingAnnualPayment(final Long sessionId) {
        if (sessionId == null) {
            return 0;
        }
        final List<GameLoan> activeLoans = gameLoanRepository
                .findAllByGameSessionIdAndLoanStatus(sessionId, LoanStatus.ACTIVE);
        return activeLoans.stream()
                .mapToInt(loan -> loan.getMonthlyPaymentAmount() * 12)
                .sum();
    }

    private int getExistingAnnualInterest(final Long sessionId) {
        if (sessionId == null) {
            return 0;
        }
        final List<GameLoan> activeLoans = gameLoanRepository
                .findAllByGameSessionIdAndLoanStatus(sessionId, LoanStatus.ACTIVE);
        return activeLoans.stream()
                .mapToInt(loan -> loan.getMonthlyPaymentAmount() * 12)
                .sum();
    }

    private double getJobCoefficient(final String jobType) {
        return switch (jobType) {
            case "LARGE_BIZ" -> 1.5;
            case "MID_BIZ" -> 1.2;
            case "SMALL_BIZ" -> 1.0;
            case "FREELANCER" -> 0.7;
            default -> 1.0;
        };
    }

    private double getCssCoefficient(final int cssGrade) {
        return switch (cssGrade) {
            case 1 -> 1.0;
            case 2 -> 0.8;
            case 3 -> 0.6;
            case 4 -> 0.4;
            case 5 -> 0.2;
            default -> 0.2;
        };
    }

    private boolean isSeoul(final String regionCode) {
        return "SEOUL".equals(regionCode);
    }
}
