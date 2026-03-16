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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "game_housings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_game_housings_session_id", columnNames = "session_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameHousing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private Long sessionId;

    @Column(name = "target_property_id", nullable = false)
    private Long targetPropertyId;

    @Column(name = "target_region", nullable = false)
    private String targetRegion;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "target_house_price", nullable = false)
    private Money targetHousePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_housing_type", nullable = false)
    private HousingType currentHousingType;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "current_deposit", nullable = false)
    private Money currentDeposit;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "monthly_rent", nullable = false)
    private Money monthlyRent;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "maintenance_fee", nullable = false)
    private Money maintenanceFee;

    private GameHousing(
        Long sessionId,
        Long targetPropertyId,
        String targetRegion,
        Money targetHousePrice,
        HousingType currentHousingType,
        Money currentDeposit,
        Money monthlyRent,
        Money maintenanceFee
    ) {
        this.sessionId = sessionId;
        this.targetPropertyId = targetPropertyId;
        this.targetRegion = targetRegion;
        this.targetHousePrice = targetHousePrice;
        this.currentHousingType = currentHousingType;
        this.currentDeposit = currentDeposit;
        this.monthlyRent = monthlyRent;
        this.maintenanceFee = maintenanceFee;
    }

    public static GameHousing create(
        Long sessionId,
        Long targetPropertyId,
        String targetRegion,
        Money targetHousePrice,
        HousingType currentHousingType,
        Money currentDeposit,
        Money monthlyRent,
        Money maintenanceFee
    ) {
        return new GameHousing(
            sessionId,
            targetPropertyId,
            targetRegion,
            targetHousePrice,
            currentHousingType,
            currentDeposit,
            monthlyRent,
            maintenanceFee
        );
    }
}
