package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.DistrictsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorldDistrictProviderServiceTest {

    @DisplayName("정상 regionCode를 입력하면 해당 지역의 district 목록만 반환한다")
    @Test
    void getDistricts() {
        // given
        final WorldDistrictProviderService worldDistrictProviderService =
            new WorldDistrictProviderService();

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("SEOUL");

        // then
        assertThat(response.getRegionCode()).isEqualTo("SEOUL");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getDistrictCode)
            .containsExactly("GANGNAM", "SONGPA", "MAPO", "GWANGJIN");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getName)
            .containsExactly("강남구", "송파구", "마포구", "광진구");
    }

    @DisplayName("다른 지역 district는 섞이지 않고 해당 regionCode의 목록만 반환한다")
    @Test
    void getDistrictsWithoutOtherRegionMixing() {
        // given
        final WorldDistrictProviderService worldDistrictProviderService =
            new WorldDistrictProviderService();

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("GWANGJU");

        // then
        assertThat(response.getRegionCode()).isEqualTo("GWANGJU");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getDistrictCode)
            .containsExactly("BUKGU");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getName)
            .containsExactly("북구");
    }

    @DisplayName("존재하지 않는 regionCode를 입력하면 입력값 예외가 발생한다")
    @Test
    void getDistrictsWithInvalidRegionCode() {
        // given
        final WorldDistrictProviderService worldDistrictProviderService =
            new WorldDistrictProviderService();

        // when & then
        assertThatThrownBy(() -> worldDistrictProviderService.getDistricts("BUSAN"))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("유효한 regionCode라도 district 데이터가 비어 있으면 빈 배열 계약을 반환한다")
    @Test
    void getDistrictsWithEmptyDistricts() {
        // given
        final WorldDistrictProviderService worldDistrictProviderService =
            new WorldDistrictProviderService(new EmptyDistrictWorldHousingSeedPolicy());

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("SEOUL");

        // then
        assertThat(response.getRegionCode()).isEqualTo("SEOUL");
        assertThat(response.getDistricts()).isEmpty();
    }

    private static class EmptyDistrictWorldHousingSeedPolicy extends WorldHousingSeedPolicy {

        @Override
        public WorldHousingSeedPlan calculate() {
            return new WorldHousingSeedPlan(
                List.of(new RegionSeed("SEOUL", "서울")),
                List.of(),
                List.of(),
                List.of()
            );
        }
    }
}
