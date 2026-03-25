package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorldHousingSeedPolicyTest {

    private final WorldHousingSeedPolicy worldHousingSeedPolicy = new WorldHousingSeedPolicy();

    @DisplayName("정책 계산 결과는 지역/구군 코드 목록과 목표 매물·문서 seed 정의를 반환한다")
    @Test
    void calculateSeedPlan() {
        // when
        final WorldHousingSeedPolicy.WorldHousingSeedPlan seedPlan = worldHousingSeedPolicy.calculate();

        // then
        assertThat(seedPlan.regionSeeds())
            .extracting(WorldHousingSeedPolicy.RegionSeed::regionCode)
            .containsExactly("SEOUL", "GWANGJU");
        assertThat(seedPlan.districtSeeds().stream()
            .filter(districtSeed -> districtSeed.regionCode().equals("SEOUL"))
            .map(WorldHousingSeedPolicy.DistrictSeed::districtCode)
            .toList())
            .containsExactly("GANGNAM", "SONGPA", "MAPO", "GWANGJIN");
        assertThat(seedPlan.propertySeeds()).isNotEmpty();
        assertThat(seedPlan.documentSeeds()).isNotEmpty();
        assertThat(seedPlan.documentSeeds())
            .extracting(WorldHousingSeedPolicy.DocumentSeed::registrySection)
            .contains(RealEstateRegistrySection.GAPGU, RealEstateRegistrySection.EULGU);
        assertThat(seedPlan.documentSeeds())
            .extracting(WorldHousingSeedPolicy.DocumentSeed::quizSamplePayload)
            .allSatisfy(payload -> {
                final RealEstateRegistryQuizSample quizSample = (RealEstateRegistryQuizSample) payload;
                assertThat(quizSample).isNotNull();
                assertThat(quizSample.getRows()).isNotEmpty();
                assertThat(quizSample.getKeyPoints()).isNotEmpty();
            });
    }

    @DisplayName("매물 seed 정의에 housingType이 없으면 예외가 발생한다")
    @Test
    void createPropertySeedsWithoutHousingType() {
        // given
        final List<Map<String, Object>> definitions = List.of(
            Map.of(
                "providerId", "PROP-HN-INVALID",
                "name", "누락된 매물",
                "address", "서울시 강남구 어딘가",
                "regionCode", "SEOUL",
                "districtCode", "GANGNAM",
                "price", 375000000,
                "latitude", 37.5172,
                "longitude", 127.0473
            )
        );

        // when & then
        assertThatThrownBy(() -> worldHousingSeedPolicy.createPropertySeeds(definitions))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @DisplayName("구군 코드 정의의 regionCode가 지역 코드 목록에 없으면 예외가 발생한다")
    @Test
    void createDistrictSeedsWithUnknownRegionCode() {
        // given
        final List<WorldHousingSeedPolicy.RegionSeed> regionSeeds = List.of(
            new WorldHousingSeedPolicy.RegionSeed("SEOUL", "서울"),
            new WorldHousingSeedPolicy.RegionSeed("GWANGJU", "광주")
        );
        final List<Map<String, Object>> definitions = List.of(
            Map.of(
                "regionCode", "BUSAN",
                "districtCode", "SUYEONG",
                "name", "수영구"
            )
        );

        // when & then
        assertThatThrownBy(() -> worldHousingSeedPolicy.createDistrictSeeds(regionSeeds, definitions))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
