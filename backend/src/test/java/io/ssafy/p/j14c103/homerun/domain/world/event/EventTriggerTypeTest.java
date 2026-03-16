package io.ssafy.p.j14c103.homerun.domain.world.event;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTriggerTypeTest {

    @DisplayName("EventTriggerType enum 값은 최신 spec과 일치한다")
    @Test
    void values() {
        assertThat(EventTriggerType.values()).containsExactly(
                EventTriggerType.PROBABILITY,
                EventTriggerType.CONDITION,
                EventTriggerType.CYCLE
        );
    }
}