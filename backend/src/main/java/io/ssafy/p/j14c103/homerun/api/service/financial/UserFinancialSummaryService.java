package io.ssafy.p.j14c103.homerun.api.service.financial;

import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHolding;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserFinancialSummaryService {

    private static final Duration INVESTMENT_PRICE_TTL = Duration.ofMinutes(5);

    private final UserAccountRepository userAccountRepository;
    private final UserFinancialProductRepository userFinancialProductRepository;
    private final UserInvestmentHoldingRepository userInvestmentHoldingRepository;
    private final UserFinancialSummaryRepository userFinancialSummaryRepository;
    private final StockMarketRepository stockMarketRepository;
    private final KisStockClient kisStockClient;

    public UserFinancialSummary getSummary(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final int cashAssetAmount = userAccountRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .mapToInt(account -> account.getBalanceSnapshot().intValue())
                .sum();

        final List<UserFinancialProduct> products = userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId);
        final int savingAssetAmount = sumProductBalance(products, FinancialProductType.SAVING_DEPOSIT);
        final int investmentAssetAmount = sumInvestmentAssetAmount(userId, products);
        final int totalDebtAmount = sumProductBalance(products, FinancialProductType.LOAN);
        final int totalAssetAmount = cashAssetAmount + savingAssetAmount + investmentAssetAmount;
        final int netAssetAmount = totalAssetAmount - totalDebtAmount;

        final UserFinancialSummary summary = userFinancialSummaryRepository.findById(userId)
                .orElseGet(() -> UserFinancialSummary.create(userId));
        summary.refresh(
                totalAssetAmount,
                totalDebtAmount,
                netAssetAmount,
                cashAssetAmount,
                savingAssetAmount,
                investmentAssetAmount
        );

        return userFinancialSummaryRepository.save(summary);
    }

    private int sumProductBalance(
            final List<UserFinancialProduct> products,
            final FinancialProductType productType
    ) {
        return products.stream()
                .filter(product -> product.getProductType() == productType)
                .mapToInt(product -> product.getCurrentBalanceAmount().intValue())
                .sum();
    }

    private int sumInvestmentAssetAmount(
            final Long userId,
            final List<UserFinancialProduct> products
    ) {
        final List<UserInvestmentHolding> holdings = userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(userId);
        if (holdings.isEmpty()) {
            return sumProductBalance(products, FinancialProductType.INVESTMENT);
        }

        final Map<String, StockMarket> stockMarkets = loadStockMarkets(holdings);
        final Map<Long, Integer> amountByProductId = new HashMap<>();
        int holdingAmount = 0;
        for (UserInvestmentHolding holding : holdings) {
            final StockMarket stockMarket = stockMarkets.get(holding.getStockCode());
            final int currentPrice = refreshCurrentPriceIfNeeded(holding, stockMarket);
            final int currentValueAmount = currentPrice * holding.getQuantity();
            holdingAmount += currentValueAmount;
            amountByProductId.merge(holding.getUserFinancialProductId(), currentValueAmount, Integer::sum);
        }

        syncInvestmentProductBalances(products, amountByProductId);
        return holdingAmount;
    }

    private Map<String, StockMarket> loadStockMarkets(final List<UserInvestmentHolding> holdings) {
        final List<String> stockCodes = holdings.stream()
                .map(UserInvestmentHolding::getStockCode)
                .distinct()
                .toList();
        final Map<String, StockMarket> stockMarketMap = new HashMap<>();
        for (StockMarket stockMarket : stockMarketRepository.findAllById(stockCodes)) {
            stockMarketMap.put(stockMarket.getStockCode(), stockMarket);
        }
        return stockMarketMap;
    }

    private int refreshCurrentPriceIfNeeded(
            final UserInvestmentHolding holding,
            final StockMarket stockMarket
    ) {
        final LocalDateTime now = LocalDateTime.now();
        if (!isPriceExpired(holding, now)) {
            return holding.getCurrentPriceAmount();
        }

        if (stockMarket != null
                && stockMarket.getKisStockCode() != null
                && !stockMarket.getKisStockCode().isBlank()) {
            final Integer realtimePrice = kisStockClient.getCurrentPrice(stockMarket.getKisStockCode());
            if (realtimePrice != null && realtimePrice > 0) {
                holding.updateCurrentPrice(realtimePrice, now);
                return realtimePrice;
            }
        }

        final int fallbackPrice = resolveFallbackPrice(holding, stockMarket);
        if (fallbackPrice > 0) {
            holding.touchPriceUpdatedAt(now);
        }
        return fallbackPrice;
    }

    private boolean isPriceExpired(
            final UserInvestmentHolding holding,
            final LocalDateTime now
    ) {
        return holding.getPriceUpdatedAt() == null
                || holding.getPriceUpdatedAt().plus(INVESTMENT_PRICE_TTL).isBefore(now);
    }

    private int resolveFallbackPrice(
            final UserInvestmentHolding holding,
            final StockMarket stockMarket
    ) {
        if (holding.getCurrentPriceAmount() != null && holding.getCurrentPriceAmount() > 0) {
            return holding.getCurrentPriceAmount();
        }
        if (stockMarket != null && stockMarket.getBasePriceAmount() != null && stockMarket.getBasePriceAmount() > 0) {
            return stockMarket.getBasePriceAmount();
        }
        return 0;
    }

    private void syncInvestmentProductBalances(
            final List<UserFinancialProduct> products,
            final Map<Long, Integer> amountByProductId
    ) {
        products.stream()
                .filter(product -> product.getProductType() == FinancialProductType.INVESTMENT)
                .forEach(product -> {
                    final Integer amount = amountByProductId.get(product.getId());
                    if (amount != null) {
                        product.updateBalance(amount);
                    }
                });
    }
}
