package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplication;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplicationRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanStatus;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private GameLoanRepository gameLoanRepository;

    @Mock
    private LoanApprovalService loanApprovalService;

    @Test
    @DisplayName("싸피론 대출 - 세션당 1건 정상 생성")
    void 싸피론_정상_생성() {
        // given
        final Integer sessionId = 1;
        final int principal = 10_000_000;

        given(gameLoanRepository.countByGameSessionIdAndProductIdAndLoanStatus(
                sessionId, "SSAFY_LOAN", LoanStatus.ACTIVE))
                .willReturn(0);
        given(gameLoanRepository.save(any(GameLoan.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final GameLoan loan = loanService.applySsafyLoan(sessionId, principal);

        // then
        assertThat(loan.getLoanName()).isEqualTo("싸피론");
        assertThat(loan.getPrincipalAmount()).isEqualTo(10_000_000);
        assertThat(loan.isActive()).isTrue();
        assertThat(loan.isSsafyLoan()).isTrue();
        verify(gameLoanRepository).save(any(GameLoan.class));
    }

    @Test
    @DisplayName("싸피론 대출 - 이미 존재하면 예외 발생")
    void 싸피론_중복_예외() {
        // given
        final Integer sessionId = 1;

        given(gameLoanRepository.countByGameSessionIdAndProductIdAndLoanStatus(
                sessionId, "SSAFY_LOAN", LoanStatus.ACTIVE))
                .willReturn(1);

        // when & then
        assertThatThrownBy(() -> loanService.applySsafyLoan(sessionId, 5_000_000))
                .isInstanceOf(HomerunException.class);
    }

    @Test
    @DisplayName("중도 상환 - 부분 상환 시 잔액 감소")
    void 중도상환_부분() {
        // given
        final Integer sessionId = 1;
        final Integer loanId = 10;
        final GameLoan loan = GameLoan.createSsafyLoan(sessionId, 10_000_000);

        given(gameLoanRepository.findById(loanId))
                .willReturn(Optional.of(loan));

        // when
        final var response = loanService.repay(sessionId, loanId, 3_000_000);

        // then
        assertThat(response.repaidAmount()).isEqualTo(3_000_000);
        assertThat(response.remainingPrincipal()).isEqualTo(7_000_000);
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    @DisplayName("중도 상환 - 전액 상환 시 CLOSED 상태")
    void 중도상환_전액_CLOSED() {
        // given
        final Integer sessionId = 1;
        final Integer loanId = 10;
        final GameLoan loan = GameLoan.createSsafyLoan(sessionId, 5_000_000);

        given(gameLoanRepository.findById(loanId))
                .willReturn(Optional.of(loan));

        // when
        final var response = loanService.repay(sessionId, loanId, 5_000_000);

        // then
        assertThat(response.remainingPrincipal()).isEqualTo(0);
        assertThat(loan.isActive()).isFalse();
    }

    @Test
    @DisplayName("이자 계산기 - 원리금균등 결과 반환")
    void 이자_계산기_원리금균등() {
        // given & when
        final var response = loanService.calculate(
                200_000_000, 3.49, 360, "EQUAL_PRINCIPAL_INTEREST");

        // then
        assertThat(response.monthlyPayment()).isGreaterThan(0);
        assertThat(response.totalInterest()).isGreaterThan(0);
        assertThat(response.totalPayment()).isGreaterThan(200_000_000);
    }
}
