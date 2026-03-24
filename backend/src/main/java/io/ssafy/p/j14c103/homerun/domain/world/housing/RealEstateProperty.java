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

    @Column(name = "provider_id", unique = true)
    private String providerId;

    @Column(name = "property_name")
    private String propertyName;

    private String address;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "legal_dong_code")
    private String legalDongCode;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "base_price_amount")
    private Money basePrice;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type")
    private HousingType housingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contract_traps")
    private List<ContractTrap> contractTraps;

    private RealEstateProperty(
        String providerId,
        String propertyName,
        String address,
        String regionCode,
        String districtCode,
        String legalDongCode,
        Money basePrice,
        BigDecimal latitude,
        BigDecimal longitude,
        PropertyType propertyType,
        TransactionType transactionType,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
        this.providerId = providerId;
        this.propertyName = propertyName;
        this.address = address;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.legalDongCode = legalDongCode;
        this.basePrice = basePrice;
        this.latitude = latitude;
        this.longitude = longitude;
        this.propertyType = propertyType;
        this.transactionType = transactionType;
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
            null,
            basePrice,
            latitude,
            longitude,
            null,
            null,
            housingType,
            contractTraps
        );
    }

    public static RealEstateProperty create(
        String providerId,
        String propertyName,
        String address,
        String regionCode,
        String districtCode,
        String legalDongCode,
        Money basePrice,
        BigDecimal latitude,
        BigDecimal longitude,
        PropertyType propertyType,
        TransactionType transactionType,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
        return new RealEstateProperty(
            providerId,
            propertyName,
            address,
            regionCode,
            districtCode,
            legalDongCode,
            basePrice,
            latitude,
            longitude,
            propertyType,
            transactionType,
            housingType,
            contractTraps
        );
    }

    public void updateFromImport(
        String propertyName,
        String address,
        String regionCode,
        String districtCode,
        String legalDongCode,
        Money basePrice,
        BigDecimal latitude,
        BigDecimal longitude,
        PropertyType propertyType,
        TransactionType transactionType,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
        this.propertyName = propertyName;
        this.address = address;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.legalDongCode = legalDongCode;
        this.basePrice = basePrice;
        this.latitude = latitude;
        this.longitude = longitude;
        this.propertyType = propertyType;
        this.transactionType = transactionType;
        this.housingType = housingType;
        this.contractTraps = contractTraps;
    }
}
