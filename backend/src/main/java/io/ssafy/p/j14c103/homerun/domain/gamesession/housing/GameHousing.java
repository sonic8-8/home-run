package io.ssafy.p.j14c103.homerun.domain.gamesession.housing;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
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
    private Long gameSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_housing_type")
    private HousingType currentHousingType;

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

    private GameHousing(
        final Long gameSessionId,
        final HousingType currentHousingType,
        final Money currentDeposit,
        final Money monthlyRent,
        final Money maintenanceFee,
        final Long currentPropertyId
    ) {
        this.gameSessionId = gameSessionId;
        this.currentHousingType = currentHousingType;
        this.currentDeposit = currentDeposit;
        this.monthlyRent = monthlyRent;
        this.maintenanceFee = maintenanceFee;
        this.currentPropertyId = currentPropertyId;
    }

    public static GameHousing create(
        final Long gameSessionId,
        final HousingType currentHousingType,
        final Money currentDeposit,
        final Money monthlyRent,
        final Money maintenanceFee,
        final Long currentPropertyId
    ) {
        return new GameHousing(
            gameSessionId,
            currentHousingType,
            currentDeposit,
            monthlyRent,
            maintenanceFee,
            currentPropertyId
        );
    }
}
