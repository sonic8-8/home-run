package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TRANSACTION_TYPE_DEPOSIT = "1";

    private final SsafyDemandDepositClient demandDepositClient;

    public DashboardResponse getDashboard(final String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }

        final Money totalAssets = calculateTotalAsset(userKey);
        final Money monthlyIncome = calculateMonthlyIncome(userKey);
        final Money monthlyExpense = calculateMonthlyExpense(userKey);

        // 전월 대비 변동 (현재는 0으로 설정 - 추후 전월 데이터 비교 구현)
        final Money incomeChange = Money.zero();
        final Money expenseChange = Money.zero();

        // 다음 월급일까지 남은 일수 (25일 기준)
        final int nextPaydayDays = calculateNextPaydayDays();

        return DashboardResponse.of(totalAssets, monthlyIncome, monthlyExpense,
                incomeChange, expenseChange, nextPaydayDays);
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

    private Money calculateTotalAsset(final String userKey) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        Money total = Money.zero();
        for (final Map<String, Object> account : accounts) {
            final long balance = parseBalance(account.get("accountBalance"));
            total = total.add(Money.of(balance));
        }
        return total;
    }

    private Money calculateMonthlyIncome(final String userKey) {
        final List<Map<String, Object>> transactions = getThisMonthTransactions(userKey);

        Money income = Money.zero();
        for (final Map<String, Object> tx : transactions) {
            if (isDeposit(tx)) {
                income = income.add(Money.of(parseBalance(tx.get("transactionBalance"))));
            }
        }
        return income;
    }

    private Money calculateMonthlyExpense(final String userKey) {
        final List<Map<String, Object>> transactions = getThisMonthTransactions(userKey);

        Money expense = Money.zero();
        for (final Map<String, Object> tx : transactions) {
            if (!isDeposit(tx)) {
                expense = expense.add(Money.of(parseBalance(tx.get("transactionBalance"))));
            }
        }
        return expense;
    }

    private List<Map<String, Object>> getThisMonthTransactions(final String userKey) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        final LocalDate now = LocalDate.now();
        final String startDate = now.withDayOfMonth(1).format(DATE_FORMATTER);
        final String endDate = now.format(DATE_FORMATTER);

        return accounts.stream()
                .map(account -> (String) account.get("accountNo"))
                .flatMap(accountNo -> demandDepositClient
                        .inquireTransactionHistory(userKey, accountNo, startDate, endDate)
                        .stream())
                .toList();
    }

    private boolean isDeposit(final Map<String, Object> transaction) {
        return TRANSACTION_TYPE_DEPOSIT.equals(String.valueOf(transaction.get("transactionType")));
    }

    private long parseBalance(final Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
