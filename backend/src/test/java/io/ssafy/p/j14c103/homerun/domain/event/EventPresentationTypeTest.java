package io.ssafy.p.j14c103.homerun.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EventPresentationTypeTest {
    @DisplayName("EventPresentationType enum 값은 최신 spec과 일치한다")
    @Test
    void values() {
        assertThat(EventPresentationType.values()).containsExactly(
                EventPresentationType.CHOICE,
                EventPresentationType.PHONE,
                EventPresentationType.JOB_TRANSFER,
                EventPresentationType.LETTER,
                EventPresentationType.GIFT
        );
    }
}