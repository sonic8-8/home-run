package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 세션 내 활성 대출.
 */
@Getter
@Entity
@Table(name = "game_loans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameLoan {

    private static final String SSAFY_LOAN_PRODUCT_ID = "SSAFY_LOAN";
    private static final String SSAFY_LOAN_NAME = "싸피론";
    private static final BigDecimal SSAFY_LOAN_RATE = new BigDecimal("2.5000");
    private static final int SSAFY_LOAN_MAX_AMOUNT = 10_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_loan_id")
    private Integer gameLoanId;

    @Column(name = "game_session_id", nullable = false)
    private Integer gameSessionId;

    @Column(name = "loan_name", length = 100)
    private String loanName;

    @Column(name = "principal_amount")
    private Integer principalAmount;

    @Column(name = "interest_rate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "monthly_payment_amount")
    private Integer monthlyPaymentAmount;

    @Column(name = "remaining_repayment_turns")
    private Integer remainingRepaymentTurns;

    @Column(name = "product_id", length = 100)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_type", length = 30)
    private RepaymentType repaymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", length = 20)
    private LoanStatus loanStatus;

    private GameLoan(
            final Integer gameSessionId,
            final String loanName,
            final Integer principalAmount,
            final BigDecimal interestRate,
            final Integer monthlyPaymentAmount,
            final Integer remainingRepaymentTurns,
            final String productId,
            final RepaymentType repaymentType
    ) {
        this.gameSessionId = gameSessionId;
        this.loanName = loanName;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.monthlyPaymentAmount = monthlyPaymentAmount;
        this.remainingRepaymentTurns = remainingRepaymentTurns;
        this.productId = productId;
        this.repaymentType = repaymentType;
        this.loanStatus = LoanStatus.ACTIVE;
    }

    public static GameLoan create(
            final Integer gameSessionId,
            final String loanName,
            final Integer principalAmount,
            final BigDecimal interestRate,
            final Integer monthlyPaymentAmount,
            final Integer remainingRepaymentTurns,
            final String productId,
            final RepaymentType repaymentType
    ) {
        return new GameLoan(gameSessionId, loanName, principalAmount, interestRate,
                monthlyPaymentAmount, remainingRepaymentTurns, productId, repaymentType);
    }

    /**
     * 싸피론 대출 생성. 고정금리 2.5%, 원리금균등.
     */
    public static GameLoan createSsafyLoan(final Integer gameSessionId, final Integer principal) {
        if (principal > SSAFY_LOAN_MAX_AMOUNT) {
            throw new IllegalArgumentException("싸피론 최대 한도는 " + SSAFY_LOAN_MAX_AMOUNT + "원입니다.");
        }
        final int monthlyInterest = calculateMonthlyInterest(principal, SSAFY_LOAN_RATE);
        return new GameLoan(gameSessionId, SSAFY_LOAN_NAME, principal, SSAFY_LOAN_RATE,
                monthlyInterest, null, SSAFY_LOAN_PRODUCT_ID,
                RepaymentType.EQUAL_PRINCIPAL_INTEREST);
    }

    /**
     * 중도 상환.
     */
    public void repay(final int amount) {
        this.principalAmount -= amount;
        if (this.principalAmount <= 0) {
            this.principalAmount = 0;
            close();
            return;
        }
        this.monthlyPaymentAmount = calculateMonthlyInterest(this.principalAmount, this.interestRate);
    }

    /**
     * 정산 Phase 2 Step 10: 월 이자 계산.
     *
     * @return 이번 턴 이자 차감액
     */
    public int chargeMonthlyInterest() {
        if (!isActive()) {
            return 0;
        }
        if (this.remainingRepaymentTurns != null) {
            this.remainingRepaymentTurns--;
            if (this.remainingRepaymentTurns <= 0) {
                close();
            }
        }
        return this.monthlyPaymentAmount;
    }

    public void close() {
        this.loanStatus = LoanStatus.CLOSED;
    }

    public boolean isActive() {
        return this.loanStatus == LoanStatus.ACTIVE;
    }

    public boolean isSsafyLoan() {
        return SSAFY_LOAN_PRODUCT_ID.equals(this.productId);
    }

    private static int calculateMonthlyInterest(final Integer principal, final BigDecimal annualRate) {
        return BigDecimal.valueOf(principal)
                .multiply(annualRate)
                .divide(BigDecimal.valueOf(1200), 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
