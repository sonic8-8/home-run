package io.ssafy.p.j14c103.homerun.domain.world.housing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "housing_regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HousingRegion {

    @Id
    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "region_name")
    private String regionName;

    private HousingRegion(
        String regionCode,
        String regionName
    ) {
        this.regionCode = regionCode;
        this.regionName = regionName;
    }

    public static HousingRegion create(
        String regionCode,
        String regionName
    ) {
        return new HousingRegion(regionCode, regionName);
    }

    public void update(String regionName) {
        this.regionName = regionName;
    }
}
