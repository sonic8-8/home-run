package io.ssafy.p.j14c103.homerun.api.service.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Mock
    private CardTransactionRepository cardTransactionRepository;

    @Mock
    private UserFinancialSummaryService userFinancialSummaryService;

    @InjectMocks
    private DashboardService dashboardService;

    @DisplayName("요약과 계좌 기준으로 총자산과 계좌 잔액을 반환한다")
    @Test
    void getDashboard_totalAssetAndBalances() {
        // given
        final Long userId = 1L;
        given(userFinancialSummaryService.getSummary(userId)).willReturn(summary(userId, 8_000_000, 0, 8_000_000));
        given(userAccountTransactionRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).willReturn(List.of());
        given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        )).willReturn(List.of());
        given(userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN))
                .willReturn(java.util.Optional.of(UserAccount.create(
                        userId, AccountType.MAIN, "001", "한국은행", "0011111111111111", 5_000_000
                )));
        given(userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY))
                .willReturn(java.util.Optional.of(UserAccount.create(
                        userId, AccountType.SEEDMONEY, "001", "한국은행", "0012222222222222", 3_000_000
                )));

        // when
        final DashboardResponse response = dashboardService.getDashboard(userId);

        // then
        assertThat(response.getTotalAssets()).isEqualTo(Money.of(8_000_000L));
        assertThat(response.getMainAccountBalance()).isEqualTo(Money.of(5_000_000L));
        assertThat(response.getSeedmoneyBalance()).isEqualTo(Money.of(3_000_000L));
    }

    @DisplayName("이번 달 입금 거래와 카드 결제 내역을 구분해 월 수입과 월 지출을 계산한다")
    @Test
    void getDashboard_monthlyIncomeAndExpense() {
        // given
        final Long userId = 1L;
        given(userFinancialSummaryService.getSummary(userId)).willReturn(summary(userId, 8_000_000, 0, 8_000_000));
        given(userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN))
                .willReturn(java.util.Optional.of(UserAccount.create(
                        userId, AccountType.MAIN, "001", "한국은행", "0011111111111111", 5_000_000
                )));
        given(userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY))
                .willReturn(java.util.Optional.of(UserAccount.create(
                        userId, AccountType.SEEDMONEY, "001", "한국은행", "0012222222222222", 3_000_000
                )));
        given(userAccountTransactionRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).willReturn(List.of(
                UserAccountTransaction.create(userId, AccountType.MAIN, null, AccountTransactionType.DEPOSIT, 3_000_000, null),
                UserAccountTransaction.create(userId, AccountType.MAIN, null, AccountTransactionType.DEPOSIT, 2_000_000, null),
                UserAccountTransaction.create(userId, AccountType.MAIN, null, AccountTransactionType.WITHDRAW, 500_000, null),
                UserAccountTransaction.create(userId, AccountType.MAIN, 10L, AccountTransactionType.PASS_SAVE_OUT, 300_000, null)
        ));
        given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        )).willReturn(List.of(
                sampleCardTransaction(userId, 120_000, LocalDate.now().minusDays(1)),
                sampleCardTransaction(userId, 80_000, LocalDate.now().minusDays(2))
        ));

        // when
        final DashboardResponse response = dashboardService.getDashboard(userId);

        // then
        assertThat(response.getMonthlyIncome()).isEqualTo(Money.of(5_000_000L));
        assertThat(response.getMonthlyExpense()).isEqualTo(Money.of(700_000L));
    }

    @DisplayName("userId가 null이면 예외가 발생한다")
    @Test
    void getDashboard_nullUserId_exception() {
        assertThatThrownBy(() -> dashboardService.getDashboard(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private UserFinancialSummary summary(
            final Long userId,
            final int totalAssetAmount,
            final int totalDebtAmount,
            final int netAssetAmount
    ) {
        final UserFinancialSummary summary = UserFinancialSummary.create(userId);
        summary.refresh(totalAssetAmount, totalDebtAmount, netAssetAmount, totalAssetAmount, 0, 0);
        return summary;
    }

    private CardTransaction sampleCardTransaction(
            final Long userId,
            final int paymentAmount,
            final LocalDate paymentDate
    ) {
        final CardProduct cardProduct = CardProduct.create(
                "Alpha Card",
                "Issuer",
                "설명",
                0,
                20_000,
                "[]",
                "alpha.png",
                true
        );
        final OwnedCard ownedCard = OwnedCard.create(
                userId,
                cardProduct,
                "주카드",
                "1111-****",
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        return CardTransaction.create(
                userId,
                ownedCard,
                "CG-1",
                "생활",
                "스타벅스",
                paymentAmount,
                paymentDate
        );
    }
}
