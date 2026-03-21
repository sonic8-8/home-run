package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.world.response.RegionsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateChecklistItem;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorldRegionProviderServiceTest {

    @DisplayName("기본 정책을 읽어 목표 지역 목록 provider 응답을 반환한다")
    @Test
    void getRegions() {
        // given
        final WorldRegionProviderService worldRegionProviderService = new WorldRegionProviderService();

        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions())
            .extracting(RegionsProviderResponse.RegionItem::getRegionCode)
            .containsExactly("SEOUL", "GWANGJU");
        assertThat(response.getRegions())
            .extracting(RegionsProviderResponse.RegionItem::getName)
            .containsExactly("서울", "광주");
    }

    @DisplayName("원천 지역 데이터가 비어 있어도 빈 배열 계약을 반환한다")
    @Test
    void getRegionsWithEmptyPlan() {
        // given
        final WorldRegionProviderService worldRegionProviderService =
            new WorldRegionProviderService(new EmptyWorldHousingSeedPolicy());

        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions()).isEmpty();
    }

    @DisplayName("provider 응답은 regionCode와 name만 노출하는 지역 목록 계약을 유지한다")
    @Test
    void getRegionsContract() {
        // given
        final WorldRegionProviderService worldRegionProviderService = new WorldRegionProviderService();

        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions()).hasSize(2);
        assertThat(response.getRegions().get(0).getRegionCode()).isEqualTo("SEOUL");
        assertThat(response.getRegions().get(0).getName()).isEqualTo("서울");
    }

    private static class EmptyWorldHousingSeedPolicy extends WorldHousingSeedPolicy {

        @Override
        public WorldHousingSeedPlan calculate() {
            return new WorldHousingSeedPlan(
                List.of(),
                List.of(),
                List.of(
                    new PropertySeed(
                        "IGNORED",
                        "무시되는 매물",
                        "서울시 어딘가",
                        "SEOUL",
                        "GANGNAM",
                        Money.of(1000L),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        HousingType.STUDIO,
                        List.of()
                    )
                ),
                List.of(
                    new DocumentSeed(
                        "IGNORED",
                        RealEstateDocumentType.CONTRACT,
                        "/ignored.png",
                        List.of(RealEstateChecklistItem.create("TRAP", "무시", false))
                    )
                )
            );
        }
    }
}
