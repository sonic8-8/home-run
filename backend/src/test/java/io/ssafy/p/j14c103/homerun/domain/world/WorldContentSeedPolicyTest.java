package io.ssafy.p.j14c103.homerun.domain.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorldContentSeedPolicyTest {

    private final WorldContentSeedPolicy worldContentSeedPolicy = new WorldContentSeedPolicy();

    @DisplayName("정책 계산 결과는 뉴스 20건과 이벤트 12건을 반환한다")
    @Test
    void calculateSeedPlan() {
        // when
        final WorldContentSeedPolicy.WorldContentSeedPlan seedPlan = worldContentSeedPolicy.calculate();

        // then
        assertThat(seedPlan.newsSeeds()).hasSize(20);
        assertThat(seedPlan.eventSeeds()).hasSize(12);
    }

    @DisplayName("뉴스 seed 정의에 sectorImpact가 없으면 예외가 발생한다")
    @Test
    void createNewsSeedsWithoutSectorImpact() {
        // given
        final List<Map<String, Object>> definitions = List.of(
            Map.of(
                "newsId", "NEWS-INVALID",
                "title", "누락된 뉴스",
                "category", "ECONOMY",
                "sentiment", "NEGATIVE",
                "realEstateImpact", -1,
                "jobImpact", Map.of("STARTUP", Map.of("salaryMult", 90))
            )
        );

        // when & then
        assertThatThrownBy(() -> worldContentSeedPolicy.createNewsSeeds(definitions))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @DisplayName("조건형 이벤트 seed 정의에 conditions가 없으면 예외가 발생한다")
    @Test
    void createEventSeedsWithoutConditions() {
        // given
        final List<Map<String, Object>> definitions = List.of(
            Map.of(
                "eventCode", "EVT-JOB-INVALID",
                "eventName", "누락된 이직 제안",
                "presentationType", "JOB_TRANSFER",
                "triggerType", "CONDITION",
                "choices", new ArrayList<>(List.of(
                    Map.of("choiceCode", "ACCEPT", "choiceName", "수락한다", "choiceOrder", 1),
                    Map.of("choiceCode", "REJECT", "choiceName", "거절한다", "choiceOrder", 2)
                ))
            )
        );

        // when & then
        assertThatThrownBy(() -> worldContentSeedPolicy.createEventSeeds(definitions))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
