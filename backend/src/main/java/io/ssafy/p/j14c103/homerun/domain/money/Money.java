package io.ssafy.p.j14c103.homerun.domain.money;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {

    private static final int SCALE = 0;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(final BigDecimal amount) {
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(final BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다.");
        }
        return new Money(amount);
    }

    public static Money of(final long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money zero() {
        return ZERO;
    }

    public Money add(final Money other) {
        if (other == null) {
            throw new IllegalArgumentException("더할 금액은 null일 수 없습니다.");
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(final Money other) {
        if (other == null) {
            throw new IllegalArgumentException("뺄 금액은 null일 수 없습니다.");
        }
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isGreaterThan(final Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @JsonValue
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    @Converter(autoApply = false)
    public static class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

        @Override
        public BigDecimal convertToDatabaseColumn(final Money money) {
            if (money == null) {
                return null;
            }
            return money.getAmount();
        }

        @Override
        public Money convertToEntityAttribute(final BigDecimal dbData) {
            if (dbData == null) {
                return null;
            }
            return Money.of(dbData);
        }
    }
}
