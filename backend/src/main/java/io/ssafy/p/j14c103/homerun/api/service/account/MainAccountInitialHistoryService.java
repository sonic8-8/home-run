package io.ssafy.p.j14c103.homerun.api.service.account;

import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MainAccountInitialHistoryService {

    private static final long BASE_SEED_OFFSET = 50_000_000L;

    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final UserAccountRepository userAccountRepository;

    public void seedInitialHistory(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new IllegalStateException("주계좌가 없습니다."));
        final long currentBalance = mainAccount.getBalanceSnapshot();
        if (currentBalance < 0) {
            throw new IllegalArgumentException("현재 잔액은 0 이상이어야 합니다.");
        }
        if (Boolean.TRUE.equals(mainAccount.getMainInitialHistorySeeded())) {
            return;
        }

        final List<UserAccountTransaction> transactions = new ArrayList<>();
        final Random expenseRandom = randomOf(userId, "MAIN_HISTORY_EXPENSE_V2");
        final Random ratioRandom = randomOf(userId, "MAIN_HISTORY_RATIO_V2");
        long currentMonthDelta = 0L;

        for (int month = 11; month >= 1; month--) {
            final LocalDate baseDate = LocalDate.now().minusMonths(month);
            final int salaryAmount = 2_900_000 + expenseRandom.nextInt(250_000);
            final int livingAmount = splitLivingAmount(salaryAmount, ratioRandom);
            final int fixedAmount = salaryAmount - livingAmount;

            transactions.add(createWithdrawal(userId, livingAmount, "생활비 지출", baseDate, 7));
            transactions.add(createWithdrawal(userId, fixedAmount, "고정비 지출", baseDate, 18));
            transactions.add(createDeposit(userId, salaryAmount, "급여 입금", baseDate, 25));
        }

        final LocalDate currentMonth = LocalDate.now();
        if (currentMonth.getDayOfMonth() >= 7) {
            final int livingAmount = 680_000 + expenseRandom.nextInt(240_000);
            transactions.add(createWithdrawal(userId, livingAmount, "생활비 지출", currentMonth, 7));
            currentMonthDelta -= livingAmount;
        }
        if (currentMonth.getDayOfMonth() >= 18) {
            final int fixedAmount = 320_000 + expenseRandom.nextInt(180_000);
            transactions.add(createWithdrawal(userId, fixedAmount, "고정비 지출", currentMonth, 18));
            currentMonthDelta -= fixedAmount;
        }
        if (currentMonth.getDayOfMonth() >= 25) {
            final long salaryAmount = Math.max(0L, -currentMonthDelta);
            transactions.add(createDeposit(userId, salaryAmount, "급여 입금", currentMonth, 25));
            currentMonthDelta += salaryAmount;
        }

        final long openingBalance = currentBalance - currentMonthDelta;
        final LocalDate openingDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        transactions.add(UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.DEPOSIT,
                openingBalance,
                null,
                "초기 자산 설정",
                null,
                openingDate.atStartOfDay()
        ));

        userAccountTransactionRepository.saveAll(transactions);
        mainAccount.markMainInitialHistorySeeded();
    }

    private UserAccountTransaction createDeposit(
            final Long userId,
            final long amount,
            final String summary,
            final LocalDate baseDate,
            final int dayOfMonth
    ) {
        return UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.DEPOSIT,
                amount,
                null,
                summary,
                null,
                atStartOfDay(baseDate, dayOfMonth)
        );
    }

    private UserAccountTransaction createWithdrawal(
            final Long userId,
            final long amount,
            final String summary,
            final LocalDate baseDate,
            final int dayOfMonth
    ) {
        return UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.WITHDRAW,
                amount,
                summary,
                summary,
                null,
                atStartOfDay(baseDate, dayOfMonth)
        );
    }

    private int splitLivingAmount(final int totalExpense, final Random random) {
        final int livingRatio = 58 + random.nextInt(18);
        return Math.max(1, totalExpense * livingRatio / 100);
    }

    private LocalDateTime atStartOfDay(final LocalDate baseDate, final int dayOfMonth) {
        return baseDate.withDayOfMonth(Math.min(dayOfMonth, baseDate.lengthOfMonth())).atStartOfDay();
    }

    private Random randomOf(final Long userId, final String key) {
        final long seed = (BASE_SEED_OFFSET + userId) * 31 + key.hashCode();
        return new Random(seed);
    }
}
