package io.ssafy.p.j14c103.homerun.domain.world.housing;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "game_housings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameHousing {

    @Id
    @Column(name = "game_session_id")
    private Integer gameSessionId;

    @Column(name = "target_region_code")
    private String targetRegionCode;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "target_house_price_amount")
    private Money targetHousePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type")
    private HousingType housingType;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "current_deposit_amount")
    private Money currentDeposit;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "monthly_rent_amount")
    private Money monthlyRent;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "maintenance_fee_amount")
    private Money maintenanceFee;

    @Column(name = "current_property_id")
    private Long currentPropertyId;

    @Column(name = "target_property_id")
    private Long targetPropertyId;

    private GameHousing(
        Integer gameSessionId,
        String targetRegionCode,
        Money targetHousePrice,
        HousingType currentHousingType,
        Money currentDeposit,
        Money monthlyRent,
        Money maintenanceFee,
        Long currentPropertyId,
        Long targetPropertyId
    ) {
        this.gameSessionId = gameSessionId;
        this.targetRegionCode = targetRegionCode;
        this.targetHousePrice = targetHousePrice;
        this.housingType = currentHousingType;
        this.currentDeposit = currentDeposit;
        this.monthlyRent = monthlyRent;
        this.maintenanceFee = maintenanceFee;
        this.currentPropertyId = currentPropertyId;
        this.targetPropertyId = targetPropertyId;
    }

    public static GameHousing create(
        Integer gameSessionId,
        String targetRegionCode,
        Money targetHousePrice,
        HousingType currentHousingType,
        Money currentDeposit,
        Money monthlyRent,
        Money maintenanceFee,
        Long currentPropertyId,
        Long targetPropertyId
    ) {
        return new GameHousing(
            gameSessionId,
            targetRegionCode,
            targetHousePrice,
            currentHousingType,
            currentDeposit,
            monthlyRent,
            maintenanceFee,
            currentPropertyId,
            targetPropertyId
        );
    }

    public HousingType getCurrentHousingType() {
        return housingType;
    }
}
