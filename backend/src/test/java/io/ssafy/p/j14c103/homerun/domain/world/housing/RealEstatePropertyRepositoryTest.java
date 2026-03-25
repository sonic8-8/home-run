package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RealEstatePropertyRepositoryTest {

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @DisplayName("RealEstateProperty를 저장하면 매물 기본 정보를 다시 조회할 수 있다.")
    @Test
    void saveRealEstateProperty() {
        // given
        RealEstateProperty property = RealEstateProperty.create(
            "PROP-SEOUL-001",
            "서울 첫 매물",
            "서울시 강남구 어딘가",
            "SEOUL",
            "GANGNAM",
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.OWNED_APT,
            List.of()
        );

        // when
        RealEstateProperty saved = realEstatePropertyRepository.save(property);

        // then
        assertThat(saved.getProviderId()).isEqualTo("PROP-SEOUL-001");
        assertThat(saved.getPropertyName()).isEqualTo("서울 첫 매물");
        assertThat(saved.getAddress()).isEqualTo("서울시 강남구 어딘가");
        assertThat(saved.getRegionCode()).isEqualTo("SEOUL");
        assertThat(saved.getDistrictCode()).isEqualTo("GANGNAM");
        assertThat(saved.getBasePrice()).isEqualTo(Money.of(375_000_000L));
        assertThat(saved.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(37.5172));
        assertThat(saved.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(127.0473));
        assertThat(saved.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }

    @DisplayName("regionCode와 districtCode로 매물을 조회할 수 있다.")
    @Test
    void findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc() {
        // given
        RealEstateProperty first = RealEstateProperty.create(
            "PROP-SEOUL-001",
            "서울 매물1",
            "서울시 강남구 어딘가",
            "SEOUL",
            "GANGNAM",
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.OWNED_APT,
            List.of()
        );

        RealEstateProperty second = RealEstateProperty.create(
            "PROP-SEOUL-002",
            "서울 매물2",
            "서울시 강남구 어딘가 2",
            "SEOUL",
            "GANGNAM",
            Money.of(280_000_000L),
            BigDecimal.valueOf(37.5150),
            BigDecimal.valueOf(127.0400),
            HousingType.JEONSE_APT,
            List.of()
        );

        RealEstateProperty otherDistrict = RealEstateProperty.create(
            "PROP-SEOUL-003",
            "다른 구 매물",
            "서울시 송파구 어딘가",
            "SEOUL",
            "SONGPA",
            Money.of(310_000_000L),
            BigDecimal.valueOf(37.5000),
            BigDecimal.valueOf(127.1000),
            HousingType.VILLA,
            List.of()
        );

        realEstatePropertyRepository.saveAll(List.of(first, second, otherDistrict));

        // when
        List<RealEstateProperty> result =
            realEstatePropertyRepository.findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
                "SEOUL",
                "GANGNAM"
            );

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RealEstateProperty::getProviderId)
            .containsExactly("PROP-SEOUL-001", "PROP-SEOUL-002");
    }

    @DisplayName("contractTraps 객체를 구조화된 JSON 객체 목록으로 저장 후 다시 조회할 수 있다.")
    @Test
    void saveStructuredContractTraps() {
        // given
        RealEstateProperty property = RealEstateProperty.create(
            "PROP-SEOUL-004",
            "함정이 있는 매물",
            "서울시 마포구 어딘가",
            "SEOUL",
            "MAPO",
            Money.of(410_000_000L),
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            HousingType.JEONSE_APT,
            List.of(
                ContractTrap.create(
                    "TRAP-01",
                    "HIGH_MORTGAGE",
                    "REGISTRY",
                    "과도한 근저당 설정 발견",
                    ContractTrapPenalty.create("-5000000", 20)
                ),
                ContractTrap.create(
                    "TRAP-02",
                    "FAKE_OWNER",
                    "CONTRACT",
                    "집주인 신분증 도용 의심",
                    ContractTrapPenalty.create("ALL_DEPOSIT_LOST", 50)
                )
            )
        );

        // when
        RealEstateProperty saved = realEstatePropertyRepository.saveAndFlush(property);
        RealEstateProperty found = realEstatePropertyRepository.findById(saved.getPropertyId())
            .orElseThrow();

        // then
        assertThat(found.getContractTraps()).hasSize(2);
        assertThat(found.getContractTraps())
            .extracting(ContractTrap::getTrapId)
            .containsExactly("TRAP-01", "TRAP-02");
        assertThat(found.getContractTraps())
            .extracting(ContractTrap::getType)
            .containsExactly("HIGH_MORTGAGE", "FAKE_OWNER");
        assertThat(found.getContractTraps().get(0).getPenalty().getCash())
            .isEqualTo("-5000000");
        assertThat(found.getContractTraps().get(1).getPenalty().getCash())
            .isEqualTo("ALL_DEPOSIT_LOST");
        assertThat(found.getContractTraps())
            .extracting(trap -> trap.getPenalty().getStress())
            .containsExactly(20, 50);
    }

    @DisplayName("대표 매물 분류값과 법정동 코드를 함께 저장 후 다시 조회할 수 있다.")
    @Test
    void savePropertyClassification() {
        // given
        RealEstateProperty property = RealEstateProperty.create(
            "PROP-SONGPA-001",
            "잠실 대표 매물",
            "서울특별시 송파구 잠실동 35",
            "11",
            "11710",
            "1171010100",
            Money.of(2_300_000_000L),
            BigDecimal.valueOf(37.5133012),
            BigDecimal.valueOf(127.1029384),
            PropertyType.APARTMENT,
            TransactionType.SALE,
            HousingType.OWNED_APT,
            List.of()
        );

        // when
        RealEstateProperty saved = realEstatePropertyRepository.saveAndFlush(property);
        RealEstateProperty found = realEstatePropertyRepository.findById(saved.getPropertyId())
            .orElseThrow();

        // then
        assertThat(found.getLegalDongCode()).isEqualTo("1171010100");
        assertThat(found.getPropertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(found.getTransactionType()).isEqualTo(TransactionType.SALE);
        assertThat(found.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }
}
