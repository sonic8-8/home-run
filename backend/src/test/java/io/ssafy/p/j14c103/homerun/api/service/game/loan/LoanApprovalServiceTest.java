package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanApprovalService.ApprovalResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LoanApprovalServiceTest {

    @Autowired
    private LoanApprovalService loanApprovalService;

    @Autowired
    private GameLoanRepository gameLoanRepository;

    @Test
    @DisplayName("개인신용대출 - 대기업 2등급이면 연봉 × 1.5 × 0.8 한도 승인")
    void 개인신용_대기업_2등급_승인() {
        // given
        final int annualSalary = 36_000_000;
        final String jobType = "LARGE_BIZ";
        final int cssGrade = 2;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.CREDIT, annualSalary, jobType, cssGrade,
                "SEOUL", null, null);

        // then
        assertThat(result.approved()).isTrue();
        assertThat(result.maxLoanAmount()).isEqualTo((int) (36_000_000 * 1.5 * 0.8));
    }

    @Test
    @DisplayName("개인신용대출 - 프리랜서 5등급은 낮은 한도 승인")
    void 개인신용_프리랜서_5등급_저한도() {
        // given
        final int annualSalary = 24_000_000;
        final String jobType = "FREELANCER";
        final int cssGrade = 5;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.CREDIT, annualSalary, jobType, cssGrade,
                "GWANGJU", null, null);

        // then
        assertThat(result.approved()).isTrue();
        assertThat(result.maxLoanAmount()).isEqualTo((int) (24_000_000 * 0.7 * 0.2));
    }

    @Test
    @DisplayName("전세대출 서울 - LTV 70% 적용")
    void 전세_서울_LTV_70() {
        // given
        final int annualSalary = 50_000_000;
        final Integer propertyPrice = 300_000_000;
        final Integer sessionId = 1;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.JEONSE, annualSalary, "LARGE_BIZ", 2,
                "SEOUL", propertyPrice, sessionId);

        // then
        assertThat(result.approved()).isTrue();
        final int ltvLimit = (int) (300_000_000 * 0.70);
        assertThat(result.maxLoanAmount()).isLessThanOrEqualTo(ltvLimit);
    }

    @Test
    @DisplayName("전세대출 광주 - LTV 80% 적용")
    void 전세_광주_LTV_80() {
        // given
        final int annualSalary = 50_000_000;
        final Integer propertyPrice = 200_000_000;
        final Integer sessionId = 1;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.JEONSE, annualSalary, "MID_BIZ", 3,
                "GWANGJU", propertyPrice, sessionId);

        // then
        assertThat(result.approved()).isTrue();
        final int ltvLimit = (int) (200_000_000 * 0.80);
        assertThat(result.maxLoanAmount()).isLessThanOrEqualTo(ltvLimit);
    }

    @Test
    @DisplayName("주택담보대출 서울 - LTV 50% 적용")
    void 주담_서울_LTV_50() {
        // given
        final int annualSalary = 60_000_000;
        final Integer propertyPrice = 500_000_000;
        final Integer sessionId = 1;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.MORTGAGE, annualSalary, "LARGE_BIZ", 1,
                "SEOUL", propertyPrice, sessionId);

        // then
        assertThat(result.approved()).isTrue();
        final int ltvLimit = (int) (500_000_000 * 0.50);
        assertThat(result.maxLoanAmount()).isLessThanOrEqualTo(ltvLimit);
    }

    @Test
    @DisplayName("주택담보대출 광주 - LTV 70%, DTI 60% 적용")
    void 주담_광주_LTV_70_DTI_60() {
        // given
        final int annualSalary = 40_000_000;
        final Integer propertyPrice = 300_000_000;
        final Integer sessionId = 1;

        // when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.MORTGAGE, annualSalary, "MID_BIZ", 2,
                "GWANGJU", propertyPrice, sessionId);

        // then
        assertThat(result.approved()).isTrue();
    }

    @Test
    @DisplayName("전세대출 - 매물 가격 없으면 거절")
    void 전세_매물없음_거절() {
        // given & when
        final ApprovalResult result = loanApprovalService.evaluate(
                LoanType.JEONSE, 50_000_000, "LARGE_BIZ", 1,
                "SEOUL", null, null);

        // then
        assertThat(result.approved()).isFalse();
        assertThat(result.rejectionReason()).contains("매물");
    }
}
