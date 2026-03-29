package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstatePropertyBoundsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyListProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class WorldRealEstatePropertyProviderServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @DisplayName("bounds 안에 있는 매물만 propertyId 오름차순으로 반환한다.")
    @Test
    void getPropertiesInBounds() {
        // given
        realEstatePropertyRepository.saveAllAndFlush(List.of(
            createProperty(
                "PROP-BOUNDARY-SW",
                "경계 남서 매물",
                BigDecimal.valueOf(37.5100),
                BigDecimal.valueOf(127.0400),
                375_000_000L
            ),
            createProperty(
                "PROP-CENTER",
                "중앙 매물",
                BigDecimal.valueOf(37.5200),
                BigDecimal.valueOf(127.0500),
                420_000_000L
            ),
            createProperty(
                "PROP-BOUNDARY-NE",
                "경계 북동 매물",
                BigDecimal.valueOf(37.5300),
                BigDecimal.valueOf(127.0600),
                580_000_000L
            ),
            createProperty(
                "PROP-OUTSIDE",
                "범위 밖 매물",
                BigDecimal.valueOf(37.5400),
                BigDecimal.valueOf(127.0700),
                610_000_000L
            )
        ));
        final RealEstatePropertyBoundsServiceRequest request = RealEstatePropertyBoundsServiceRequest.of(
            BigDecimal.valueOf(37.5100),
            BigDecimal.valueOf(127.0400),
            BigDecimal.valueOf(37.5300),
            BigDecimal.valueOf(127.0600)
        );

        // when
        final RealEstatePropertyListProviderResponse response =
            worldRealEstatePropertyProviderService.getPropertiesInBounds(request);

        // then
        assertThat(response.getProperties())
            .extracting(
                RealEstatePropertyListProviderResponse.PropertySummary::getName,
                RealEstatePropertyListProviderResponse.PropertySummary::getRecentPrice,
                RealEstatePropertyListProviderResponse.PropertySummary::getLatitude,
                RealEstatePropertyListProviderResponse.PropertySummary::getLongitude
            )
            .containsExactly(
                tuple(
                    "경계 남서 매물",
                    375_000_000L,
                    BigDecimal.valueOf(37.5100),
                    BigDecimal.valueOf(127.0400)
                ),
                tuple(
                    "중앙 매물",
                    420_000_000L,
                    BigDecimal.valueOf(37.5200),
                    BigDecimal.valueOf(127.0500)
                ),
                tuple(
                    "경계 북동 매물",
                    580_000_000L,
                    BigDecimal.valueOf(37.5300),
                    BigDecimal.valueOf(127.0600)
                )
            );
    }

    @DisplayName("잘못된 bounds 입력이면 INVALID_INPUT_VALUE 예외가 발생한다.")
    @Test
    void getPropertiesInBoundsWithInvalidBounds() {
        // when & then
        assertThatThrownBy(() -> RealEstatePropertyBoundsServiceRequest.of(
            BigDecimal.valueOf(37.5300),
            BigDecimal.valueOf(127.0600),
            BigDecimal.valueOf(37.5100),
            BigDecimal.valueOf(127.0400)
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("존재하는 propertyId로 상세 조회 시 현재 스키마 기반 필드를 반환한다.")
    @Test
    void getPropertyDetail() {
        // given
        final RealEstateProperty saved = realEstatePropertyRepository.saveAndFlush(createProperty(
            "PROP-DETAIL",
            "서초아트자이",
            BigDecimal.valueOf(37.4855510),
            BigDecimal.valueOf(127.0115000),
            1_300_000_000L
        ));

        // when
        final RealEstatePropertyDetailProviderResponse response =
            worldRealEstatePropertyProviderService.getPropertyDetail(saved.getPropertyId());

        // then
        assertThat(response.getPropertyId()).isEqualTo(saved.getPropertyId());
        assertThat(response.getName()).isEqualTo("서초아트자이");
        assertThat(response.getRecentPrice()).isEqualTo(1_300_000_000L);
        assertThat(response.getAddress()).isEqualTo("서울특별시 서초구 반포대로 58");
        assertThat(response.getLatitude()).isEqualByComparingTo("37.485551");
        assertThat(response.getLongitude()).isEqualByComparingTo("127.011500");
        assertThat(response.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }

    @DisplayName("존재하지 않는 propertyId로 상세 조회하면 HOUSING_PROPERTY_NOT_FOUND 예외가 발생한다.")
    @Test
    void getPropertyDetailWithUnknownProperty() {
        // when & then
        assertThatThrownBy(() -> worldRealEstatePropertyProviderService.getPropertyDetail(9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    private RealEstateProperty createProperty(
        final String providerId,
        final String propertyName,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final long price
    ) {
        return RealEstateProperty.create(
            providerId,
            propertyName,
            "서울특별시 서초구 반포대로 58",
            "11",
            "11650",
            Money.of(price),
            latitude,
            longitude,
            HousingType.OWNED_APT,
            List.of()
        );
    }
}
