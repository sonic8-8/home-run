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
@Table(name = "housing_districts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HousingDistrict {

    @Id
    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "district_legal_dong_code")
    private String districtLegalDongCode;

    private HousingDistrict(
        String districtCode,
        String regionCode,
        String districtName,
        String districtLegalDongCode
    ) {
        this.districtCode = districtCode;
        this.regionCode = regionCode;
        this.districtName = districtName;
        this.districtLegalDongCode = districtLegalDongCode;
    }

    public static HousingDistrict create(
        String districtCode,
        String regionCode,
        String districtName,
        String districtLegalDongCode
    ) {
        return new HousingDistrict(
            districtCode,
            regionCode,
            districtName,
            districtLegalDongCode
        );
    }

    public void update(
        String regionCode,
        String districtName,
        String districtLegalDongCode
    ) {
        this.regionCode = regionCode;
        this.districtName = districtName;
        this.districtLegalDongCode = districtLegalDongCode;
    }
}
