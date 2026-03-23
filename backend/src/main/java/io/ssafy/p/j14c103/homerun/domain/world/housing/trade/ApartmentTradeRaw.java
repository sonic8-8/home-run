package io.ssafy.p.j14c103.homerun.domain.world.housing.trade;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "apartment_trade_raws")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApartmentTradeRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apartment_trade_raw_id")
    private Long apartmentTradeRawId;

    @Column(name = "trade_key", unique = true)
    private String tradeKey;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "legal_dong_name")
    private String legalDongName;

    @Column(name = "legal_dong_code")
    private String legalDongCode;

    @Column(name = "apartment_name")
    private String apartmentName;

    private String jibun;

    @Column(name = "deal_date")
    private LocalDate dealDate;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "deal_amount")
    private Money dealAmount;

    @Column(name = "exclusive_area", precision = 10, scale = 4)
    private BigDecimal exclusiveArea;

    private int floor;

    @Column(name = "build_year")
    private int buildYear;

    @Column(name = "land_leasehold")
    private boolean landLeasehold;

    private ApartmentTradeRaw(
        String tradeKey,
        String districtCode,
        String legalDongName,
        String legalDongCode,
        String apartmentName,
        String jibun,
        LocalDate dealDate,
        Money dealAmount,
        BigDecimal exclusiveArea,
        int floor,
        int buildYear,
        boolean landLeasehold
    ) {
        this.tradeKey = tradeKey;
        this.districtCode = districtCode;
        this.legalDongName = legalDongName;
        this.legalDongCode = legalDongCode;
        this.apartmentName = apartmentName;
        this.jibun = jibun;
        this.dealDate = dealDate;
        this.dealAmount = dealAmount;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.buildYear = buildYear;
        this.landLeasehold = landLeasehold;
    }

    public static ApartmentTradeRaw create(
        String tradeKey,
        String districtCode,
        String legalDongName,
        String legalDongCode,
        String apartmentName,
        String jibun,
        LocalDate dealDate,
        Money dealAmount,
        BigDecimal exclusiveArea,
        int floor,
        int buildYear,
        boolean landLeasehold
    ) {
        return new ApartmentTradeRaw(
            tradeKey,
            districtCode,
            legalDongName,
            legalDongCode,
            apartmentName,
            jibun,
            dealDate,
            dealAmount,
            exclusiveArea,
            floor,
            buildYear,
            landLeasehold
        );
    }
}
