package io.ssafy.p.j14c103.homerun.domain.world.housing;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "real_estate_properties")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "property_name")
    private String propertyName;

    private String address;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_code")
    private String districtCode;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "base_price_amount")
    private Money basePrice;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type")
    private HousingType housingType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contract_traps")
    private List<ContractTrap> contractTraps;

    private RealEstateProperty(
        String providerId,
        String propertyName,
        String address,
        String regionCode,
        String districtCode,
        Money basePrice,
        BigDecimal latitude,
        BigDecimal longitude,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
        this.providerId = providerId;
        this.propertyName = propertyName;
        this.address = address;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.basePrice = basePrice;
        this.latitude = latitude;
        this.longitude = longitude;
        this.housingType = housingType;
        this.contractTraps = contractTraps;
    }

    public static RealEstateProperty create(
        String providerId,
        String propertyName,
        String address,
        String regionCode,
        String districtCode,
        Money basePrice,
        BigDecimal latitude,
        BigDecimal longitude,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
        return new RealEstateProperty(
            providerId,
            propertyName,
            address,
            regionCode,
            districtCode,
            basePrice,
            latitude,
            longitude,
            housingType,
            contractTraps
        );
    }
}
