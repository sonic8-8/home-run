package io.ssafy.p.j14c103.homerun.api.service.financial;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductTemplate;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductTemplateRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialTransactionType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialTransaction;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHolding;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserFinancialMockDataService {

    private static final long BASE_SEED_OFFSET = 50_000_000L;
    private static final Duration INVESTMENT_PRICE_TTL = Duration.ofMinutes(5);
    private static final int MIN_INVESTMENT_HOLDING_COUNT = 3;
    private static final int MAX_INVESTMENT_HOLDING_COUNT = 8;
    private static final int LOAN_CREATION_RATE = 30;
    private static final int INVESTMENT_BUY_EVENT_COUNT = 6;
    private static final String DEFAULT_LIVING_CATEGORY_ID = "CG-9ca85f66311a23d";
    private static final String DEFAULT_LIVING_CATEGORY_NAME = "생활";
    private static final String DEFAULT_TRANSPORT_CATEGORY_ID = "CG-4fa85f6455cad4a";
    private static final String DEFAULT_TRANSPORT_CATEGORY_NAME = "교통";
    private static final String DEFAULT_TELECOM_CATEGORY_ID = "CG-7fa85f6425bc311";
    private static final String DEFAULT_TELECOM_CATEGORY_NAME = "통신";

    private final FinancialProductTemplateRepository financialProductTemplateRepository;
    private final UserFinancialProductRepository userFinancialProductRepository;
    private final UserInvestmentHoldingRepository userInvestmentHoldingRepository;
    private final UserFinancialTransactionRepository userFinancialTransactionRepository;
    private final StockMarketRepository stockMarketRepository;
    private final KisStockClient kisStockClient;
    private final CardProductRepository cardProductRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;
    private final ObjectMapper objectMapper;

    public void createInitialData(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (userFinancialProductRepository.existsByUserIdAndActiveYnTrue(userId)
                || ownedCardRepository.existsByUserIdAndActiveYnTrue(userId)) {
            userFinancialSummaryService.getSummary(userId);
            return;
        }

        createSavingProduct(userId);
        createInvestmentProduct(userId);
        if (shouldCreateLoanProduct(userId)) {
            createLoanProduct(userId);
        }
        createOwnedCards(userId);
        createAccountTransactions(userId);
        userFinancialSummaryService.getSummary(userId);
    }

    private void createSavingProduct(final Long userId) {
        final FinancialProductTemplate template = pickTemplate(userId, FinancialProductType.SAVING_DEPOSIT, 0);
        if (template == null) {
            return;
        }

        final Random random = randomOf(userId, "SAVING_V1");
        final LocalDateTime openedAt = LocalDate.now().minusMonths(15).withDayOfMonth(5).atStartOfDay();
        int balance = 0;
        final List<Integer> monthlyDeposits = new ArrayList<>();
        for (int month = 11; month >= 0; month--) {
            final int amount = 300_000 + random.nextInt(250_000);
            monthlyDeposits.add(amount);
            balance += amount;
        }
        final int interestAmount = 80_000 + random.nextInt(90_000);
        balance += interestAmount;

        final UserFinancialProduct product = userFinancialProductRepository.save(UserFinancialProduct.create(
                userId,
                FinancialProductType.SAVING_DEPOSIT,
                template.getInstitutionName(),
                template.getProductName(),
                balance,
                openedAt
        ));

        int index = 0;
        for (int month = 11; month >= 0; month--) {
            userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                    userId,
                    product.getId(),
                    FinancialTransactionType.DEPOSIT,
                    monthlyDeposits.get(index++),
                    LocalDate.now().minusMonths(month).withDayOfMonth(5).atStartOfDay()
            ));
        }
        userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                userId,
                product.getId(),
                FinancialTransactionType.INTEREST,
                interestAmount,
                LocalDate.now().minusMonths(1).withDayOfMonth(28).atStartOfDay()
        ));
    }

    private void createInvestmentProduct(final Long userId) {
        final FinancialProductTemplate template = pickTemplate(userId, FinancialProductType.INVESTMENT, 1);
        if (template == null) {
            return;
        }

        final List<InvestmentHoldingSeed> holdingSeeds = buildInvestmentHoldingSeeds(userId);
        if (holdingSeeds.isEmpty()) {
            createLegacyInvestmentProduct(userId, template);
            return;
        }

        final LocalDateTime openedAt = LocalDate.now().minusMonths(12).withDayOfMonth(2).atStartOfDay();
        final int currentBalance = holdingSeeds.stream()
                .mapToInt(InvestmentHoldingSeed::currentValueAmount)
                .sum();
        final int purchaseAmount = holdingSeeds.stream()
                .mapToInt(InvestmentHoldingSeed::purchaseAmount)
                .sum();
        final Random dividendRandom = randomOf(userId, "INVESTMENT_DIVIDEND_V1");
        final int dividendAmount = Math.max(15_000, purchaseAmount * (1 + dividendRandom.nextInt(3)) / 100);

        final UserFinancialProduct product = userFinancialProductRepository.save(UserFinancialProduct.create(
                userId,
                FinancialProductType.INVESTMENT,
                template.getInstitutionName(),
                template.getProductName(),
                currentBalance,
                openedAt
        ));

        holdingSeeds.forEach(seed -> userInvestmentHoldingRepository.save(UserInvestmentHolding.create(
                userId,
                product.getId(),
                seed.stockCode(),
                seed.quantity(),
                seed.averagePurchasePriceAmount(),
                seed.currentPriceAmount(),
                seed.priceUpdatedAt()
        )));

        final List<Integer> buyAmounts = splitAmount(
                purchaseAmount,
                INVESTMENT_BUY_EVENT_COUNT,
                randomOf(userId, "INVESTMENT_BUY_SPLIT_V1")
        );
        final int[] buyMonths = {10, 8, 6, 4, 2, 1};
        for (int index = 0; index < buyAmounts.size(); index++) {
            userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                    userId,
                    product.getId(),
                    FinancialTransactionType.BUY,
                    buyAmounts.get(index),
                    LocalDate.now().minusMonths(buyMonths[index]).withDayOfMonth(10).atStartOfDay()
            ));
        }
        userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                userId,
                product.getId(),
                FinancialTransactionType.DIVIDEND,
                dividendAmount,
                LocalDate.now().minusMonths(2).withDayOfMonth(20).atStartOfDay()
        ));
    }

    private void createLegacyInvestmentProduct(
            final Long userId,
            final FinancialProductTemplate template
    ) {
        final Random random = randomOf(userId, "INVESTMENT_LEGACY_V1");
        final LocalDateTime openedAt = LocalDate.now().minusMonths(12).withDayOfMonth(2).atStartOfDay();
        int investedAmount = 0;
        final List<Integer> buyAmounts = new ArrayList<>();
        for (int month = 9; month >= 0; month--) {
            final int amount = 150_000 + random.nextInt(180_000);
            buyAmounts.add(amount);
            investedAmount += amount;
        }
        final int dividendAmount = 30_000 + random.nextInt(30_000);
        final int currentBalance = investedAmount + dividendAmount + 120_000 + random.nextInt(220_000);

        final UserFinancialProduct product = userFinancialProductRepository.save(UserFinancialProduct.create(
                userId,
                FinancialProductType.INVESTMENT,
                template.getInstitutionName(),
                template.getProductName(),
                currentBalance,
                openedAt
        ));

        int index = 0;
        for (int month = 9; month >= 0; month--) {
            userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                    userId,
                    product.getId(),
                    FinancialTransactionType.BUY,
                    buyAmounts.get(index++),
                    LocalDate.now().minusMonths(month).withDayOfMonth(10).atStartOfDay()
            ));
        }
        userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                userId,
                product.getId(),
                FinancialTransactionType.DIVIDEND,
                dividendAmount,
                LocalDate.now().minusMonths(2).withDayOfMonth(20).atStartOfDay()
        ));
    }

    private void createLoanProduct(final Long userId) {
        final FinancialProductTemplate template = pickTemplate(userId, FinancialProductType.LOAN, 2);
        if (template == null) {
            return;
        }

        final Random random = randomOf(userId, "LOAN_V1");
        final LocalDateTime openedAt = LocalDate.now().minusMonths(20).withDayOfMonth(15).atStartOfDay();
        final int currentBalance = 5_000_000 + random.nextInt(4_000_000);
        final UserFinancialProduct product = userFinancialProductRepository.save(UserFinancialProduct.create(
                userId,
                FinancialProductType.LOAN,
                template.getInstitutionName(),
                template.getProductName(),
                currentBalance,
                openedAt
        ));

        for (int month = 11; month >= 0; month--) {
            final int repaymentAmount = 180_000 + random.nextInt(70_000);
            userFinancialTransactionRepository.save(UserFinancialTransaction.create(
                    userId,
                    product.getId(),
                    FinancialTransactionType.REPAYMENT,
                    repaymentAmount,
                    LocalDate.now().minusMonths(month).withDayOfMonth(21).atStartOfDay()
            ));
        }
    }

    private boolean shouldCreateLoanProduct(final Long userId) {
        return randomOf(userId, "LOAN_FLAG_V1").nextInt(100) < LOAN_CREATION_RATE;
    }

    private void createOwnedCards(final Long userId) {
        final List<CardProduct> cardProducts = cardProductRepository.findByActiveYnTrueOrderByCardNameAsc();
        if (cardProducts.isEmpty()) {
            return;
        }

        final int cardCount = Math.min(2, cardProducts.size());
        for (int index = 0; index < cardCount; index++) {
            final CardProduct cardProduct = cardProducts.get(index);
            final LocalDateTime openedAt = LocalDate.now()
                    .minusMonths(10L - index * 3L)
                    .withDayOfMonth(1)
                    .atStartOfDay();
            final OwnedCard ownedCard = ownedCardRepository.save(OwnedCard.create(
                    userId,
                    cardProduct,
                    cardProduct.getCardName() + " 메인",
                    maskCardNumber(userId, index),
                    openedAt
            ));
            createCardTransactions(userId, ownedCard, cardProduct, randomOf(userId, "CARD_TX_V1_" + index), index);
        }
    }

    private void createCardTransactions(
            final Long userId,
            final OwnedCard ownedCard,
            final CardProduct cardProduct,
            final Random random,
            final int offset
    ) {
        final List<CardCategory> categories = resolveCategories(cardProduct);

        for (int month = 11; month >= 0; month--) {
            final LocalDate baseDate = LocalDate.now().minusMonths(month);

            final CardCategory first = categories.get((offset + month) % categories.size());
            cardTransactionRepository.save(CardTransaction.create(
                    userId,
                    ownedCard,
                    first.categoryId(),
                    first.categoryName(),
                    first.categoryName() + " 가맹점",
                    55_000 + random.nextInt(90_000),
                    baseDate.withDayOfMonth(Math.min(5, baseDate.lengthOfMonth()))
            ));

            final CardCategory second = categories.get((offset + month + 1) % categories.size());
            cardTransactionRepository.save(CardTransaction.create(
                    userId,
                    ownedCard,
                    second.categoryId(),
                    second.categoryName(),
                    second.categoryName() + " 결제",
                    25_000 + random.nextInt(70_000),
                    baseDate.withDayOfMonth(Math.min(14, baseDate.lengthOfMonth()))
            ));
        }
    }

    private void createAccountTransactions(final Long userId) {
        final Random random = randomOf(userId, "ACCOUNT_V1");
        for (int month = 11; month >= 0; month--) {
            final LocalDate baseDate = LocalDate.now().minusMonths(month);

            userAccountTransactionRepository.save(UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.DEPOSIT,
                    2_900_000 + random.nextInt(250_000),
                    null,
                    baseDate.withDayOfMonth(Math.min(25, baseDate.lengthOfMonth())).atStartOfDay()
            ));

            userAccountTransactionRepository.save(UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.WITHDRAW,
                    680_000 + random.nextInt(240_000),
                    "생활비 지출",
                    baseDate.withDayOfMonth(Math.min(7, baseDate.lengthOfMonth())).atStartOfDay()
            ));

            userAccountTransactionRepository.save(UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.WITHDRAW,
                    320_000 + random.nextInt(180_000),
                    "고정비 지출",
                    baseDate.withDayOfMonth(Math.min(18, baseDate.lengthOfMonth())).atStartOfDay()
            ));
        }
    }

    private FinancialProductTemplate pickTemplate(
            final Long userId,
            final FinancialProductType productType,
            final int offset
    ) {
        final List<FinancialProductTemplate> templates =
                financialProductTemplateRepository.findAllByProductTypeAndActiveYnTrueOrderByIdAsc(productType);
        if (templates.isEmpty()) {
            return null;
        }

        final Random random = randomOf(userId, "TEMPLATE_" + productType.name() + "_V1_" + offset);
        return templates.get(random.nextInt(templates.size()));
    }

    private List<InvestmentHoldingSeed> buildInvestmentHoldingSeeds(final Long userId) {
        final List<StockMarket> stocks = new ArrayList<>(stockMarketRepository.findAllByKisStockCodeIsNotNullOrderByStockCodeAsc());
        if (stocks.isEmpty()) {
            stocks.addAll(stockMarketRepository.findAllByOrderByStockCodeAsc());
        }

        stocks.removeIf(this::isInvalidStockSnapshot);
        if (stocks.isEmpty()) {
            return List.of();
        }

        Collections.shuffle(stocks, randomOf(userId, "HOLDING_PICK_V1"));
        final int holdingCount = Math.min(resolveInvestmentHoldingCount(userId), stocks.size());
        final List<InvestmentHoldingSeed> holdingSeeds = new ArrayList<>();
        for (int index = 0; index < holdingCount; index++) {
            final StockMarket stock = stocks.get(index);
            final RealtimePrice realtimePrice = resolveRealtimePrice(stock);
            final int currentPrice = realtimePrice.price();
            final int quantity = generateQuantity(currentPrice, randomOf(userId, "HOLDING_QTY_V1_" + index));
            final int averagePrice = generateAveragePurchasePrice(
                    stock,
                    currentPrice,
                    randomOf(userId, "HOLDING_PRICE_V1_" + stock.getStockCode())
            );
            holdingSeeds.add(new InvestmentHoldingSeed(
                    stock.getStockCode(),
                    quantity,
                    averagePrice,
                    currentPrice,
                    realtimePrice.priceUpdatedAt()
            ));
        }

        return holdingSeeds;
    }

    private int resolveInvestmentHoldingCount(final Long userId) {
        final Random random = randomOf(userId, "HOLDING_COUNT_V1");
        return MIN_INVESTMENT_HOLDING_COUNT
                + random.nextInt(MAX_INVESTMENT_HOLDING_COUNT - MIN_INVESTMENT_HOLDING_COUNT + 1);
    }

    private RealtimePrice resolveRealtimePrice(final StockMarket stock) {
        final LocalDateTime now = LocalDateTime.now();
        if (stock.getKisStockCode() != null && !stock.getKisStockCode().isBlank()) {
            final Integer realtimePrice = kisStockClient.getCurrentPrice(stock.getKisStockCode());
            if (realtimePrice != null && realtimePrice > 0) {
                return new RealtimePrice(realtimePrice, now);
            }
        }
        return new RealtimePrice(stock.getBasePriceAmount(), now.minus(INVESTMENT_PRICE_TTL));
    }

    private boolean isInvalidStockSnapshot(final StockMarket stock) {
        return stock.getStockCode() == null
                || stock.getStockCode().isBlank()
                || stock.getBasePriceAmount() == null
                || stock.getBasePriceAmount() <= 0;
    }

    private int generateQuantity(final int currentPrice, final Random random) {
        if (currentPrice >= 200_000) {
            return 1 + random.nextInt(4);
        }
        if (currentPrice >= 100_000) {
            return 2 + random.nextInt(7);
        }
        if (currentPrice >= 50_000) {
            return 4 + random.nextInt(12);
        }
        return 8 + random.nextInt(23);
    }

    private int generateAveragePurchasePrice(
            final StockMarket stock,
            final int currentPrice,
            final Random random
    ) {
        final double targetReturn = -0.12 + (random.nextDouble() * 0.30);
        final int rawAveragePrice = (int) Math.round(currentPrice / (1 + targetReturn));
        final int yearLow = resolveYearLowPrice(stock, currentPrice);
        final int yearHigh = resolveYearHighPrice(stock, currentPrice);
        return Math.max(yearLow, Math.min(rawAveragePrice, yearHigh));
    }

    private int resolveYearLowPrice(final StockMarket stock, final int currentPrice) {
        if (stock.getYearLowPriceAmount() != null && stock.getYearLowPriceAmount() > 0) {
            return stock.getYearLowPriceAmount();
        }
        return Math.max(1, (int) Math.round(currentPrice * 0.7));
    }

    private int resolveYearHighPrice(final StockMarket stock, final int currentPrice) {
        if (stock.getYearHighPriceAmount() != null && stock.getYearHighPriceAmount() > 0) {
            return stock.getYearHighPriceAmount();
        }
        return Math.max(currentPrice, (int) Math.round(currentPrice * 1.25));
    }

    private List<Integer> splitAmount(final int totalAmount, final int count, final Random random) {
        if (count <= 0) {
            return List.of();
        }
        if (totalAmount <= count) {
            final List<Integer> amounts = new ArrayList<>();
            int remaining = totalAmount;
            for (int index = 0; index < count; index++) {
                final int amount = index == count - 1 ? remaining : Math.max(1, remaining - (count - index - 1));
                amounts.add(amount);
                remaining -= amount;
            }
            return amounts;
        }

        final List<Integer> amounts = new ArrayList<>();
        int remaining = totalAmount;
        for (int index = 0; index < count; index++) {
            final int remainingCount = count - index;
            if (remainingCount == 1) {
                amounts.add(remaining);
                break;
            }

            final int average = remaining / remainingCount;
            final int variance = Math.max(1, average / 4);
            final int minAllowed = 1;
            final int maxAllowed = remaining - (remainingCount - 1);
            int candidate = average - variance + random.nextInt(variance * 2 + 1);
            if (candidate < minAllowed) {
                candidate = minAllowed;
            }
            if (candidate > maxAllowed) {
                candidate = maxAllowed;
            }

            amounts.add(candidate);
            remaining -= candidate;
        }
        return amounts;
    }

    private Random randomOf(final Long userId, final String key) {
        final long baseSeed = BASE_SEED_OFFSET + userId;
        return new Random(baseSeed * 31L + key.hashCode());
    }

    private String maskCardNumber(final Long userId, final int index) {
        final String suffix = String.format("%04d", (userId * 10 + index) % 10_000);
        return "1234-****-****-" + suffix;
    }

    private List<CardCategory> resolveCategories(final CardProduct cardProduct) {
        if (cardProduct.getActiveBenefits() == null || cardProduct.getActiveBenefits().isBlank()) {
            return defaultCategories();
        }

        try {
            final List<Map<String, Object>> payloads = objectMapper.readValue(
                    cardProduct.getActiveBenefits(),
                    new TypeReference<>() {
                    }
            );
            final List<CardCategory> categories = payloads.stream()
                    .map(payload -> new CardCategory(
                            String.valueOf(payload.getOrDefault("categoryId", DEFAULT_LIVING_CATEGORY_ID)),
                            String.valueOf(payload.getOrDefault("categoryName", DEFAULT_LIVING_CATEGORY_NAME))
                    ))
                    .distinct()
                    .toList();
            if (!categories.isEmpty()) {
                return categories;
            }
        } catch (Exception ignored) {
            return defaultCategories();
        }

        return defaultCategories();
    }

    private List<CardCategory> defaultCategories() {
        return List.of(
                new CardCategory(DEFAULT_LIVING_CATEGORY_ID, DEFAULT_LIVING_CATEGORY_NAME),
                new CardCategory(DEFAULT_TRANSPORT_CATEGORY_ID, DEFAULT_TRANSPORT_CATEGORY_NAME),
                new CardCategory(DEFAULT_TELECOM_CATEGORY_ID, DEFAULT_TELECOM_CATEGORY_NAME)
        );
    }

    private record InvestmentHoldingSeed(
            String stockCode,
            int quantity,
            int averagePurchasePriceAmount,
            int currentPriceAmount,
            LocalDateTime priceUpdatedAt
    ) {
        private int currentValueAmount() {
            return currentPriceAmount * quantity;
        }

        private int purchaseAmount() {
            return averagePurchasePriceAmount * quantity;
        }
    }

    private record RealtimePrice(int price, LocalDateTime priceUpdatedAt) {
    }

    private record CardCategory(String categoryId, String categoryName) {
    }
}
