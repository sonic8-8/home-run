package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyCreditCardClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpendingService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SsafyCreditCardClient creditCardClient;
    private final SsafyDemandDepositClient demandDepositClient;
    private final UserAuthContextService userAuthContextService;

    public SpendingResponse getSpending(final Long userId, final String month) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        final LocalDate targetMonth = parseMonth(month);
        final String resolvedMonth = targetMonth.format(MONTH_FORMATTER);
        final String startDate = targetMonth.withDayOfMonth(1).format(DATE_FORMATTER);
        final String endDate = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth()).format(DATE_FORMATTER);

        final Map<SpendingCategory, Money> categoryMap = new EnumMap<>(SpendingCategory.class);

        aggregateCardSpending(userKey, startDate, endDate, categoryMap);
        aggregateDepositWithdrawals(userKey, startDate, endDate, categoryMap);

        final Money totalExpense = categoryMap.values().stream()
                .reduce(Money.zero(), Money::add);

        final List<SpendingCategoryDetail> details = categoryMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getAmount().compareTo(a.getValue().getAmount()))
                .map(entry -> SpendingCategoryDetail.of(entry.getKey(), entry.getValue(), totalExpense))
                .collect(Collectors.toList());

        return SpendingResponse.of(resolvedMonth, totalExpense, details);
    }

    private void aggregateCardSpending(
            final String userKey,
            final String startDate,
            final String endDate,
            final Map<SpendingCategory, Money> categoryMap) {

        final List<Map<String, Object>> cards = creditCardClient.inquireSignUpCreditCardList(userKey);

        for (final Map<String, Object> card : cards) {
            final String cardNo = (String) card.get("cardNo");
            final String cvc = (String) card.get("cvc");

            final List<Map<String, Object>> transactions = creditCardClient.inquireCreditCardTransactionList(userKey,
                    cardNo, cvc, startDate, endDate);

            for (final Map<String, Object> tx : transactions) {
                final String categoryName = (String) tx.get("categoryName");
                final SpendingCategory category = SpendingCategory.from(categoryName);
                final long amount = Long.parseLong(String.valueOf(tx.get("transactionBalance")));

                categoryMap.merge(category, Money.of(amount), Money::add);
            }
        }
    }

    private void aggregateDepositWithdrawals(
            final String userKey,
            final String startDate,
            final String endDate,
            final Map<SpendingCategory, Money> categoryMap) {

        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        for (final Map<String, Object> account : accounts) {
            final String accountNo = (String) account.get("accountNo");

            final List<Map<String, Object>> transactions = demandDepositClient.inquireTransactionHistory(userKey,
                    accountNo, startDate, endDate);

            for (final Map<String, Object> tx : transactions) {
                final String txType = String.valueOf(tx.get("transactionType"));
                if (!"2".equals(txType)) {
                    continue;
                }

                final long amount = Long.parseLong(String.valueOf(tx.get("transactionBalance")));
                categoryMap.merge(SpendingCategory.TRANSFER, Money.of(amount), Money::add);
            }
        }
    }

    private LocalDate parseMonth(final String month) {
        if (month == null || month.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(month + "01", DATE_FORMATTER);
    }
}
