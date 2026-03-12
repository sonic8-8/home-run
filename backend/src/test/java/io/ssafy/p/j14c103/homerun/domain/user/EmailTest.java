package io.ssafy.p.j14c103.homerun.domain.user;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @DisplayName("정상 이메일이면 Email을 생성한다.")
    @Test
    void create() {
        // given
        Email email = Email.of("test@ssafy.com");

        // when & then
        assertThat(email.getValue()).isEqualTo("test@ssafy.com");
    }

    @DisplayName("이메일이 null이면 예외가 발생한다.")
    @Test
    void createWithNull() {
        // given & when & then
        assertThatThrownBy(() -> Email.of(null))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EMAIL_REQUIRED);
    }

    @DisplayName("이메일이 공백이면 예외가 발생한다.")
    @Test
    void createWithBlank() {
        // given & when & then
        assertThatThrownBy(() -> Email.of(" "))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EMAIL_BLANK);
    }

    @DisplayName("이메일 형식이 아니면 예외가 발생한다.")
    @Test
    void createWithInvalidFormat() {
        // given & when & then
        assertThatThrownBy(() -> Email.of("invalid-email"))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT);
    }
}
