package io.ssafy.p.j14c103.homerun.domain.card;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardProductTest {

    @DisplayName("카드명 없이 카드를 생성하면 ErrorCode 기반 예외가 발생한다")
    @Test
    void create_cardNameRequired() {
        assertThatThrownBy(() -> CardProduct.create(
                " ",
                "Issuer",
                "설명",
                300000,
                40000,
                "[]",
                "card.png",
                true
        ))
                .isInstanceOf(HomerunException.class)
                .extracting(exception -> ((HomerunException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CARD_NAME_REQUIRED);
    }
}
