package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SpendingServiceTest {

    @Mock
    private CardTransactionRepository cardTransactionRepository;

    @Mock
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @InjectMocks
    private SpendingService spendingService;

  @DisplayName("카드 결제 내역을 카테고리별로 집계한다")
  @Test
  void getSpending_cardCategories() {
    // given
    final Long userId = 1L;
    given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
            userId,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
    )).willReturn(List.of(
            sampleCardTransaction("생활", "CG-9ca85f66311a23d", 350_000, LocalDate.of(2026, 3, 3)),
            sampleCardTransaction("생활", "CG-9ca85f66311a23d", 150_000, LocalDate.of(2026, 3, 10)),
            sampleCardTransaction("교통", "CG-4fa85f6455cad4a", 120_000, LocalDate.of(2026, 3, 15))
    ));
    given(userAccountTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            userId,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999999999)
    )).willReturn(List.of());

    // when
    final SpendingResponse response = spendingService.getSpending(userId, "202603");

    // then
    assertThat(response.getTotalExpense()).isEqualTo(Money.of(620000L));
    assertThat(response.getCategories()).hasSize(2);

        final SpendingCategoryDetail living = response.getCategories().stream()
                .filter(d -> "생활".equals(d.getCategoryName()))
                .findFirst().orElseThrow();
        assertThat(living.getAmount()).isEqualTo(Money.of(500000L));
    }

  @DisplayName("수시입출금 출금은 이체 카테고리로 집계한다")
  @Test
  void getSpending_transferFromDeposit() {
    // given
    final Long userId = 1L;
    given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
            userId,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
    )).willReturn(List.of());
    given(userAccountTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            userId,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999999999)
    )).willReturn(List.of(
            UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.WITHDRAW,
                    500_000,
                    null,
                    LocalDateTime.of(2026, 3, 5, 0, 0)
            ),
            UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.WITHDRAW,
                    300_000,
                    null,
                    LocalDateTime.of(2026, 3, 12, 0, 0)
            ),
            UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.DEPOSIT,
                    1_000_000,
                    null,
                    LocalDateTime.of(2026, 3, 25, 0, 0)
            )
    ));

    // when
    final SpendingResponse response = spendingService.getSpending(userId, "202603");

    // then
    assertThat(response.getTotalExpense()).isEqualTo(Money.of(800000L));
    assertThat(response.getCategories()).hasSize(1);
    assertThat(response.getCategories().get(0).getCategoryName()).isEqualTo("이체");
    }

  @DisplayName("카드 + 입출금 합산하여 총 지출을 반환한다")
  @Test
  void getSpending_combined() {
    // given
    final Long userId = 1L;
    given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
            userId,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31)
    )).willReturn(List.of(
            sampleCardTransaction("대형마트", "CG-9ca85f66311a23d", 200_000, LocalDate.of(2026, 3, 8))
    ));
    given(userAccountTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            userId,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999999999)
    )).willReturn(List.of(
            UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.WITHDRAW,
                    500_000,
                    null,
                    LocalDateTime.of(2026, 3, 9, 0, 0)
            )
    ));

    // when
    final SpendingResponse response = spendingService.getSpending(userId, "202603");

    // then
    assertThat(response.getTotalExpense()).isEqualTo(Money.of(700000L));
    assertThat(response.getCategories()).hasSize(2);
  }

  @DisplayName("userId가 null이면 예외가 발생한다")
  @Test
  void getSpending_nullUserId_exception() {
    // when & then
    assertThatThrownBy(() -> spendingService.getSpending(null, "202603"))
            .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("month가 null이면 현재 월로 조회한다")
  @Test
  void getSpending_nullMonth_currentMonth() {
    // given
    final Long userId = 1L;
    given(cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
            org.mockito.ArgumentMatchers.eq(userId),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
    )).willReturn(List.of());
    given(userAccountTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            org.mockito.ArgumentMatchers.eq(userId),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
    )).willReturn(List.of());

    // when
    final SpendingResponse response = spendingService.getSpending(userId, null);

    // then
    assertThat(response.getTotalExpense()).isEqualTo(Money.zero());
    assertThat(response.getCategories()).isEmpty();
  }

  private CardTransaction sampleCardTransaction(
          final String categoryName,
          final String categoryId,
          final int amount,
          final LocalDate paymentDate) {
    final CardProduct cardProduct = CardProduct.create(
            "테스트 카드",
            "테스트 카드사",
            "테스트 카드",
            0,
            0,
            "[]",
            null,
            true);
    final OwnedCard ownedCard = OwnedCard.create(
            1L,
            cardProduct,
            "메인카드",
            "1234-****-****-5678",
            paymentDate.atStartOfDay());
    return CardTransaction.create(
            1L,
            ownedCard,
            categoryId,
            categoryName,
            "테스트 가맹점",
            amount,
            paymentDate);
  }
}
