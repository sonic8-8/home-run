package io.ssafy.p.j14c103.homerun.api.service.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductSourceType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHolding;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserFinancialSummaryServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserFinancialProductRepository userFinancialProductRepository;

    @Mock
    private UserInvestmentHoldingRepository userInvestmentHoldingRepository;

    @Mock
    private UserFinancialSummaryRepository userFinancialSummaryRepository;

    @Mock
    private StockMarketRepository stockMarketRepository;

    @Mock
    private KisStockClient kisStockClient;

    @InjectMocks
    private UserFinancialSummaryService userFinancialSummaryService;

    @DisplayName("최근 5분 이내에 갱신된 주식 현재가는 저장값을 그대로 사용한다")
    @Test
    void getSummary_usesStoredPriceWhenFresh() {
        // given
        final Long userId = 1L;
        final UserFinancialProduct savingProduct = financialProduct(11L, userId, FinancialProductType.SAVING_DEPOSIT, 2_000_000);
        final UserFinancialProduct investmentProduct = financialProduct(12L, userId, FinancialProductType.INVESTMENT, 1_000_000);
        final UserFinancialProduct loanProduct = financialProduct(13L, userId, FinancialProductType.LOAN, 500_000);
        final UserInvestmentHolding holding = UserInvestmentHolding.create(
                userId,
                12L,
                "005930",
                3,
                180_000,
                186_200,
                LocalDateTime.now().minusMinutes(3)
        );

        given(userAccountRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(accounts(userId, 5_000_000, 300_000));
        given(userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId))
                .willReturn(List.of(savingProduct, investmentProduct, loanProduct));
        given(userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of(holding));
        given(stockMarketRepository.findAllById(anyIterable())).willReturn(List.of(stock("005930", "005930", 186_200, 52_900, 223_000)));
        given(userFinancialSummaryRepository.findById(userId)).willReturn(Optional.empty());
        given(userFinancialSummaryRepository.save(any(UserFinancialSummary.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        // then
        assertThat(summary.getCashAssetAmount()).isEqualTo(5_300_000);
        assertThat(summary.getSavingAssetAmount()).isEqualTo(2_000_000);
        assertThat(summary.getInvestmentAssetAmount()).isEqualTo(558_600);
        assertThat(summary.getTotalDebtAmount()).isEqualTo(500_000);
        assertThat(summary.getTotalAssetAmount()).isEqualTo(7_858_600);
        assertThat(summary.getNetAssetAmount()).isEqualTo(7_358_600);
        assertThat(investmentProduct.getCurrentBalanceAmount()).isEqualTo(558_600);
        verifyNoInteractions(kisStockClient);
    }

    @DisplayName("주식 현재가가 5분을 넘기면 KIS 현재가로 갱신해 총자산에 반영한다")
    @Test
    void getSummary_refreshesPriceWhenExpired() {
        // given
        final Long userId = 2L;
        final UserFinancialProduct investmentProduct = financialProduct(21L, userId, FinancialProductType.INVESTMENT, 0);
        final UserInvestmentHolding holding = UserInvestmentHolding.create(
                userId,
                21L,
                "005930",
                2,
                170_000,
                180_000,
                LocalDateTime.now().minusMinutes(10)
        );
        final LocalDateTime beforeUpdate = holding.getPriceUpdatedAt();

        given(userAccountRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(accounts(userId, 3_000_000, 0));
        given(userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of(investmentProduct));
        given(userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of(holding));
        given(stockMarketRepository.findAllById(anyIterable())).willReturn(List.of(stock("005930", "005930", 180_000, 52_900, 223_000)));
        given(kisStockClient.getCurrentPrice("005930")).willReturn(186_200);
        given(userFinancialSummaryRepository.findById(userId)).willReturn(Optional.empty());
        given(userFinancialSummaryRepository.save(any(UserFinancialSummary.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        // then
        assertThat(holding.getCurrentPriceAmount()).isEqualTo(186_200);
        assertThat(holding.getPriceUpdatedAt()).isAfter(beforeUpdate);
        assertThat(investmentProduct.getCurrentBalanceAmount()).isEqualTo(372_400);
        assertThat(summary.getInvestmentAssetAmount()).isEqualTo(372_400);
        assertThat(summary.getTotalAssetAmount()).isEqualTo(3_372_400);
        then(kisStockClient).should().getCurrentPrice("005930");
    }

    @DisplayName("현재가 재조회에 실패하면 저장된 현재가로 총자산을 계산한다")
    @Test
    void getSummary_fallsBackToStoredPriceWhenKisFails() {
        // given
        final Long userId = 3L;
        final UserFinancialProduct investmentProduct = financialProduct(31L, userId, FinancialProductType.INVESTMENT, 0);
        final UserInvestmentHolding holding = UserInvestmentHolding.create(
                userId,
                31L,
                "035420",
                4,
                205_000,
                212_500,
                LocalDateTime.now().minusMinutes(9)
        );
        final LocalDateTime beforeUpdate = holding.getPriceUpdatedAt();

        given(userAccountRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(accounts(userId, 1_000_000, 200_000));
        given(userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of(investmentProduct));
        given(userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of(holding));
        given(stockMarketRepository.findAllById(anyIterable())).willReturn(List.of(stock("035420", "035420", 212_500, 176_200, 295_000)));
        given(kisStockClient.getCurrentPrice("035420")).willReturn(null);
        given(userFinancialSummaryRepository.findById(userId)).willReturn(Optional.empty());
        given(userFinancialSummaryRepository.save(any(UserFinancialSummary.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        // then
        assertThat(holding.getCurrentPriceAmount()).isEqualTo(212_500);
        assertThat(holding.getPriceUpdatedAt()).isAfter(beforeUpdate);
        assertThat(investmentProduct.getCurrentBalanceAmount()).isEqualTo(850_000);
        assertThat(summary.getInvestmentAssetAmount()).isEqualTo(850_000);
        assertThat(summary.getTotalAssetAmount()).isEqualTo(2_050_000);
        then(kisStockClient).should().getCurrentPrice("035420");
    }

    @DisplayName("투자 보유 상세가 없으면 기존 투자상품 잔액을 그대로 사용한다")
    @Test
    void getSummary_usesInvestmentProductBalanceWhenHoldingsMissing() {
        // given
        final Long userId = 4L;
        final UserFinancialProduct investmentProduct = financialProduct(41L, userId, FinancialProductType.INVESTMENT, 1_500_000);
        final UserFinancialProduct savingProduct = financialProduct(42L, userId, FinancialProductType.SAVING_DEPOSIT, 2_500_000);

        given(userAccountRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(accounts(userId, 2_000_000, 500_000));
        given(userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId))
                .willReturn(List.of(investmentProduct, savingProduct));
        given(userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(userId)).willReturn(List.of());
        given(userFinancialSummaryRepository.findById(userId)).willReturn(Optional.empty());
        given(userFinancialSummaryRepository.save(any(UserFinancialSummary.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);

        // then
        assertThat(summary.getCashAssetAmount()).isEqualTo(2_500_000);
        assertThat(summary.getSavingAssetAmount()).isEqualTo(2_500_000);
        assertThat(summary.getInvestmentAssetAmount()).isEqualTo(1_500_000);
        assertThat(summary.getTotalDebtAmount()).isZero();
        assertThat(summary.getTotalAssetAmount()).isEqualTo(6_500_000);
        assertThat(summary.getNetAssetAmount()).isEqualTo(6_500_000);
        verifyNoInteractions(stockMarketRepository, kisStockClient);
    }

    private List<UserAccount> accounts(
            final Long userId,
            final int mainBalance,
            final int seedmoneyBalance
    ) {
        return List.of(
                UserAccount.create(userId, AccountType.MAIN, "001", "한국은행", "0011111111111111", mainBalance),
                UserAccount.create(userId, AccountType.SEEDMONEY, "001", "한국은행", "0012222222222222", seedmoneyBalance)
        );
    }

    private UserFinancialProduct financialProduct(
            final Long id,
            final Long userId,
            final FinancialProductType productType,
            final int currentBalanceAmount
    ) {
        final UserFinancialProduct product = UserFinancialProduct.create(
                userId,
                productType,
                "기관",
                productType.name() + " 상품",
                currentBalanceAmount,
                LocalDateTime.now().minusMonths(12),
                FinancialProductSourceType.SYSTEM
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private StockMarket stock(
            final String stockCode,
            final String kisStockCode,
            final int basePriceAmount,
            final int yearLowPriceAmount,
            final int yearHighPriceAmount
    ) {
        return StockMarket.create(
                stockCode,
                stockCode,
                kisStockCode,
                "섹터",
                basePriceAmount,
                yearLowPriceAmount,
                yearHighPriceAmount,
                BigDecimal.valueOf(0.02)
        );
    }
}
