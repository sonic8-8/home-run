package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LoanServiceTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private GameLoanRepository gameLoanRepository;

    @Test
    @DisplayName("싸피론 대출 - 세션당 1건 정상 생성")
    void applySsafyLoan() {
        // given
        final Long sessionId = 1L;
        final int principal = 10_000_000;

        // when
        final GameLoan loan = loanService.applySsafyLoan(sessionId, principal);

        // then
        assertThat(loan.getLoanName()).isEqualTo("싸피론");
        assertThat(loan.getPrincipalAmount()).isEqualTo(10_000_000);
        assertThat(loan.isActive()).isTrue();
        assertThat(loan.isSsafyLoan()).isTrue();
        assertThat(gameLoanRepository.findById(loan.getGameLoanId())).isPresent();
    }

    @Test
    @DisplayName("싸피론 대출 - 이미 존재하면 예외 발생")
    void applySsafyLoanDuplicateThrows() {
        // given
        final Long sessionId = 1L;
        gameLoanRepository.save(GameLoan.createSsafyLoan(sessionId, 5_000_000));

        // when & then
        assertThatThrownBy(() -> loanService.applySsafyLoan(sessionId, 5_000_000))
                .isInstanceOf(HomerunException.class);
    }

    @Test
    @DisplayName("중도 상환 - 부분 상환 시 잔액 감소")
    void repayPartial() {
        // given
        final Long sessionId = 1L;
        final GameLoan loan = gameLoanRepository.save(GameLoan.createSsafyLoan(sessionId, 10_000_000));
        final Integer loanId = loan.getGameLoanId();

        // when
        final var response = loanService.repay(sessionId, loanId, 3_000_000);

        // then
        assertThat(response.repaidAmount()).isEqualTo(3_000_000);
        assertThat(response.remainingPrincipal()).isEqualTo(7_000_000);
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    @DisplayName("중도 상환 - 전액 상환 시 CLOSED 상태")
    void repayFullClosed() {
        // given
        final Long sessionId = 1L;
        final GameLoan loan = gameLoanRepository.save(GameLoan.createSsafyLoan(sessionId, 5_000_000));
        final Integer loanId = loan.getGameLoanId();

        // when
        final var response = loanService.repay(sessionId, loanId, 5_000_000);

        // then
        assertThat(response.remainingPrincipal()).isEqualTo(0);
        assertThat(loan.isActive()).isFalse();
    }

    @Test
    @DisplayName("이자 계산기 - 원리금균등 결과 반환")
    void calculateEqualPrincipalInterest() {
        // given & when
        final var response = loanService.calculate(
                200_000_000, 3.49, 360, "EQUAL_PRINCIPAL_INTEREST");

        // then
        assertThat(response.monthlyPayment()).isGreaterThan(0);
        assertThat(response.totalInterest()).isGreaterThan(0);
        assertThat(response.totalPayment()).isGreaterThan(200_000_000);
    }
}
