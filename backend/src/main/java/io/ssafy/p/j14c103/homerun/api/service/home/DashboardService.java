package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.account.UserSsafyAccountSyncService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;
    private final UserSsafyAccountSyncService userSsafyAccountSyncService;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;

    public DashboardResponse getDashboard(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        userSsafyAccountSyncService.syncLinkedAccounts(userId);
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);
        final Optional<UserAssetProfile> assetProfile = userAssetProfileRepository.findById(userId);
        final Money totalAssets = Money.of(summary.getTotalAssetAmount());
        final Money monthlyIncome = assetProfile
                .map(profile -> Money.of(profile.getMonthlySalaryAmount() + getOtherIncomeAmount(userId)))
                .orElseGet(() -> calculateMonthlyIncome(userId));
        final Money monthlyExpense = assetProfile
                .map(profile -> Money.of(profile.getMonthlyFixedExpenseAmount() + getCardSpendAmount(userId)))
                .orElseGet(() -> calculateMonthlyExpense(userId));
        final Money incomeChange = Money.zero();
        final Money expenseChange = Money.zero();
        final int nextPaydayDays = calculateNextPaydayDays(assetProfile.map(UserAssetProfile::getSalaryDayOfMonth).orElse(25));
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

    private int calculateNextPaydayDays(final int payday) {
        final LocalDate today = LocalDate.now();
        LocalDate nextPayday = today.withDayOfMonth(payday);
        if (!today.isBefore(nextPayday)) {
            nextPayday = nextPayday.plusMonths(1);
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, nextPayday);
    }

    private Money calculateMonthlyIncome(final Long userId) {
        final LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        final long amount = userAccountTransactionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, monthStart.atStartOfDay())
                .stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.DEPOSIT)
                .mapToLong(transaction -> transaction.getAmount().longValue())
                .sum();

        return Money.of(amount);
    }

    private Money calculateMonthlyExpense(final Long userId) {
        final LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        final long accountExpense = userAccountTransactionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, monthStart.atStartOfDay())
                .stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.WITHDRAW)
                .mapToLong(transaction -> transaction.getAmount().longValue())
                .sum();
        final long cardExpense = cardTransactionRepository
                .findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(userId, monthStart, LocalDate.now())
                .stream()
                .mapToLong(transaction -> transaction.getPaymentAmount().longValue())
                .sum();

        return Money.of(accountExpense + cardExpense);
    }

    private long getOtherIncomeAmount(final Long userId) {
        return userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private long getCardSpendAmount(final Long userId) {
        return userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private long findAccountBalance(final Long userId, final AccountType accountType) {
        return userAccountRepository.findByUserIdAndAccountType(userId, accountType)
                .map(account -> account.getBalanceSnapshot())
                .orElse(0L);
    }
}
