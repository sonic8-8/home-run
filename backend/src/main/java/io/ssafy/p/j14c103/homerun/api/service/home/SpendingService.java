package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.account.UserSsafyAccountSyncService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final CardTransactionRepository cardTransactionRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final UserSsafyAccountSyncService userSsafyAccountSyncService;

    public SpendingResponse getSpending(final Long userId, final String month) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        userSsafyAccountSyncService.syncLinkedAccounts(userId);
        final LocalDate targetMonth = parseMonth(month);
        final String resolvedMonth = targetMonth.format(MONTH_FORMATTER);
        final LocalDate startDate = targetMonth.withDayOfMonth(1);
        final LocalDate endDate = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        final Map<SpendingCategory, Money> categoryMap = new EnumMap<>(SpendingCategory.class);

        aggregateCardSpending(userId, startDate, endDate, categoryMap);
        aggregateAccountWithdrawals(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay().minusNanos(1),
                categoryMap
        );

        final Money totalExpense = categoryMap.values().stream()
                .reduce(Money.zero(), Money::add);

        final List<SpendingCategoryDetail> details = categoryMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().getAmount().compareTo(a.getValue().getAmount()))
                .map(entry -> SpendingCategoryDetail.of(entry.getKey(), entry.getValue(), totalExpense))
                .collect(Collectors.toList());

        return SpendingResponse.of(resolvedMonth, totalExpense, details);
    }

    private void aggregateCardSpending(
            final Long userId,
            final LocalDate startDate,
            final LocalDate endDate,
            final Map<SpendingCategory, Money> categoryMap) {

        cardTransactionRepository.findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(
                        userId,
                        startDate,
                        endDate
                )
                .forEach(transaction -> {
                    final SpendingCategory category = SpendingCategory.from(transaction.getCategoryName());
                    categoryMap.merge(category, Money.of(transaction.getPaymentAmount()), Money::add);
                });
    }

    private void aggregateAccountWithdrawals(
            final Long userId,
            final LocalDateTime startDateTime,
            final LocalDateTime endDateTime,
            final Map<SpendingCategory, Money> categoryMap) {
        userAccountTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        userId,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .filter(this::isExternalExpense)
                .forEach(transaction -> categoryMap.merge(
                        SpendingCategory.TRANSFER,
                        Money.of(transaction.getAmount()),
                        Money::add
                ));
    }

    private boolean isExternalExpense(final UserAccountTransaction transaction) {
        return transaction.getTransactionType() == AccountTransactionType.WITHDRAW;
    }

    private LocalDate parseMonth(final String month) {
        if (month == null || month.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(month + "01", DATE_FORMATTER);
    }
}
