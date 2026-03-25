package io.ssafy.p.j14c103.homerun.domain.account;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public class AccountId {

    private final String value;

    private AccountId(final String value) {
        this.value = value;
    }

    public static AccountId of(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 null이거나 빈 값일 수 없습니다.");
        }
        return new AccountId(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountId accountId)) {
            return false;
        }
        return Objects.equals(value, accountId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
