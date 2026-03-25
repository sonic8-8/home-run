package io.ssafy.p.j14c103.homerun.domain.user;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String value;

    private Email(String value) {
        validate(value);
        this.value = value;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    private void validate(String value) {
        validateRequired(value);
        validatePattern(value);
    }

    private void validateRequired(String value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.EMAIL_REQUIRED);
        }
        if (value.isBlank()) {
            throw new HomerunException(ErrorCode.EMAIL_BLANK);
        }
    }

    private void validatePattern(String value) {
        if (EMAIL_PATTERN.matcher(value).matches()) {
            return;
        }

        throw new HomerunException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Email email)) {
            return false;
        }
        return Objects.equals(value, email.value);
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
