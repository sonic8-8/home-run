package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

class ProfileMonthlyExpensePolicy {

    private static final BigDecimal MONTHLY_EXPENSE_RATIO = new BigDecimal("0.555");

    Money calculate(final Money monthlyIncome) {
        final BigDecimal amount = monthlyIncome.getAmount()
            .multiply(MONTHLY_EXPENSE_RATIO)
            .setScale(0, RoundingMode.HALF_UP);
        return Money.of(amount);
    }
}
