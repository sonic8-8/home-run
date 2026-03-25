package io.ssafy.p.j14c103.homerun.domain.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatAutoChangePolicyTest {

    private final StatAutoChangePolicy policy = new StatAutoChangePolicy();

    @DisplayName("자동 스탯 변화는 체력을 매 턴 2 감소시킨다.")
    @Test
    void calculateHealthDelta() {
        // given
        final GameStat gameStat = createGameStat(80, 20, 20, 50, 50);

        // when
        final StatAutoChangePolicy.StatAutoChange result = policy.calculate(
            gameStat,
            HousingType.VILLA
        );

        // then
        assertThat(result.healthDelta()).isEqualTo(-2);
    }

    @DisplayName("지식이 61 이상이면 자동 감소량은 3이다.")
    @Test
    void calculateHighKnowledgeDecay() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 61);

        // when
        final StatAutoChangePolicy.StatAutoChange result = policy.calculate(
            gameStat,
            HousingType.VILLA
        );

        // then
        assertThat(result.knowledgeDelta()).isEqualTo(-3);
    }

    @DisplayName("지식이 31 이상 60 이하이면 자동 감소량은 2다.")
    @Test
    void calculateMidKnowledgeDecay() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 31);

        // when
        final StatAutoChangePolicy.StatAutoChange result = policy.calculate(
            gameStat,
            HousingType.VILLA
        );

        // then
        assertThat(result.knowledgeDelta()).isEqualTo(-2);
    }

    @DisplayName("지식이 30 이하이면 자동 감소량은 1이다.")
    @Test
    void calculateLowKnowledgeDecay() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 30);

        // when
        final StatAutoChangePolicy.StatAutoChange result = policy.calculate(
            gameStat,
            HousingType.VILLA
        );

        // then
        assertThat(result.knowledgeDelta()).isEqualTo(-1);
    }

    @DisplayName("행복도 공식은 실수 계산 결과를 내림해 스트레스 감소량으로 반영한다.")
    @Test
    void calculateStressReliefByHappiness() {
        // given
        final GameStat veryHappy = createGameStat(70, 20, 20, 100, 50);
        final GameStat happy = createGameStat(70, 20, 20, 75, 50);
        final GameStat normal = createGameStat(70, 20, 20, 50, 50);
        final GameStat low = createGameStat(70, 20, 20, 25, 50);
        final GameStat empty = createGameStat(70, 20, 20, 0, 50);

        // when
        final StatAutoChangePolicy.StatAutoChange veryHappyResult = policy.calculate(
            veryHappy,
            HousingType.VILLA
        );
        final StatAutoChangePolicy.StatAutoChange happyResult = policy.calculate(
            happy,
            HousingType.VILLA
        );
        final StatAutoChangePolicy.StatAutoChange normalResult = policy.calculate(
            normal,
            HousingType.VILLA
        );
        final StatAutoChangePolicy.StatAutoChange lowResult = policy.calculate(
            low,
            HousingType.VILLA
        );
        final StatAutoChangePolicy.StatAutoChange emptyResult = policy.calculate(
            empty,
            HousingType.VILLA
        );

        // then
        assertThat(veryHappyResult.stressDelta()).isEqualTo(-4);
        assertThat(happyResult.stressDelta()).isEqualTo(-3);
        assertThat(normalResult.stressDelta()).isEqualTo(-2);
        assertThat(lowResult.stressDelta()).isEqualTo(-1);
        assertThat(emptyResult.stressDelta()).isZero();
    }

    @DisplayName("주거 유형별 피로도와 스트레스 보정이 정확하다.")
    @Test
    void calculateHousingTypeAdjustment() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 0, 50);

        // when
        final StatAutoChangePolicy.StatAutoChange noneResult = policy.calculate(
            gameStat,
            HousingType.NONE
        );
        final StatAutoChangePolicy.StatAutoChange studioResult = policy.calculate(
            gameStat,
            HousingType.STUDIO
        );
        final StatAutoChangePolicy.StatAutoChange villaResult = policy.calculate(
            gameStat,
            HousingType.VILLA
        );
        final StatAutoChangePolicy.StatAutoChange jeonseResult = policy.calculate(
            gameStat,
            HousingType.JEONSE_APT
        );
        final StatAutoChangePolicy.StatAutoChange ownedResult = policy.calculate(
            gameStat,
            HousingType.OWNED_APT
        );

        // then
        assertThat(noneResult.fatigueDelta()).isEqualTo(-4);
        assertThat(noneResult.stressDelta()).isEqualTo(15);
        assertThat(studioResult.fatigueDelta()).isEqualTo(-5);
        assertThat(studioResult.stressDelta()).isEqualTo(5);
        assertThat(villaResult.fatigueDelta()).isEqualTo(-8);
        assertThat(villaResult.stressDelta()).isZero();
        assertThat(jeonseResult.fatigueDelta()).isEqualTo(-11);
        assertThat(jeonseResult.stressDelta()).isEqualTo(-3);
        assertThat(ownedResult.fatigueDelta()).isEqualTo(-13);
        assertThat(ownedResult.stressDelta()).isEqualTo(-6);
    }

    @DisplayName("자동 스탯 변화는 주거 보정과 행복도 공식을 합산해 스트레스를 계산한다.")
    @Test
    void calculateCombinedStressDelta() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 75, 90);

        // when
        final StatAutoChangePolicy.StatAutoChange result = policy.calculate(
            gameStat,
            HousingType.NONE
        );

        // then
        assertThat(result).isEqualTo(
            new StatAutoChangePolicy.StatAutoChange(-2, -4, 12, 0, -3)
        );
    }

    @DisplayName("주거 유형이 없으면 예외가 발생한다.")
    @Test
    void calculateWithNullHousingType() {
        // given
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 50);

        // when & then
        assertThatThrownBy(() -> policy.calculate(gameStat, null))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameStat createGameStat(
        final int health,
        final int fatigue,
        final int stress,
        final int happiness,
        final int knowledge
    ) {
        return GameStat.create(1, health, fatigue, stress, happiness, knowledge, 1);
    }
}
