package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;

    public DashboardResponse getDashboard(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);
        final Money totalAssets = Money.of(summary.getTotalAssetAmount().longValue());
        final Money monthlyIncome = calculateMonthlyIncome(userId);
        final Money monthlyExpense = calculateMonthlyExpense(userId);
        final Money incomeChange = Money.zero();
        final Money expenseChange = Money.zero();
        final int nextPaydayDays = calculateNextPaydayDays();
        final Money mainAccountBalance = Money.of(findAccountBalance(userId, AccountType.MAIN));
        final Money seedmoneyBalance = Money.of(findAccountBalance(userId, AccountType.SEEDMONEY));

        return DashboardResponse.of(
                totalAssets,
                monthlyIncome,
                monthlyExpense,
                incomeChange,
                expenseChange,
                nextPaydayDays,
                mainAccountBalance,
                seedmoneyBalance
        );
    }

    private int calculateNextPaydayDays() {
        final LocalDate today = LocalDate.now();
        final int payday = 25;
        LocalDate nextPayday = today.withDayOfMonth(payday);
        if (!today.isBefore(nextPayday)) {
            nextPayday = nextPayday.plusMonths(1);
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, nextPayday);
    }

    private Money calculateMonthlyIncome(final Long userId) {
        final LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        final int amount = userAccountTransactionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, monthStart.atStartOfDay())
                .stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.DEPOSIT)
                .mapToInt(transaction -> transaction.getAmount().intValue())
                .sum();

        return Money.of(amount);
    }

    private Money calculateMonthlyExpense(final Long userId) {
        final LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        final int accountExpense = userAccountTransactionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, monthStart.atStartOfDay())
                .stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.WITHDRAW)
                .mapToInt(transaction -> transaction.getAmount().intValue())
                .sum();
        final int cardExpense = cardTransactionRepository
                .findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(userId, monthStart, LocalDate.now())
                .stream()
                .mapToInt(transaction -> transaction.getPaymentAmount().intValue())
                .sum();

        return Money.of(accountExpense + cardExpense);
    }

    private long findAccountBalance(final Long userId, final AccountType accountType) {
        return userAccountRepository.findByUserIdAndAccountType(userId, accountType)
                .map(account -> account.getBalanceSnapshot().longValue())
                .orElse(0L);
    }
}
