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
@Table(name = "housing_legal_dongs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HousingLegalDong {

    @Id
    @Column(name = "legal_dong_code")
    private String legalDongCode;

    @Column(name = "parent_legal_dong_code")
    private String parentLegalDongCode;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "legal_dong_name")
    private String legalDongName;

    @Column(name = "full_address_name")
    private String fullAddressName;

    private HousingLegalDong(
        String legalDongCode,
        String parentLegalDongCode,
        String regionCode,
        String districtCode,
        String legalDongName,
        String fullAddressName
    ) {
        this.legalDongCode = legalDongCode;
        this.parentLegalDongCode = parentLegalDongCode;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.legalDongName = legalDongName;
        this.fullAddressName = fullAddressName;
    }

    public static HousingLegalDong create(
        String legalDongCode,
        String parentLegalDongCode,
        String regionCode,
        String districtCode,
        String legalDongName,
        String fullAddressName
    ) {
        return new HousingLegalDong(
            legalDongCode,
            parentLegalDongCode,
            regionCode,
            districtCode,
            legalDongName,
            fullAddressName
        );
    }

    public void update(
        String parentLegalDongCode,
        String regionCode,
        String districtCode,
        String legalDongName,
        String fullAddressName
    ) {
        this.parentLegalDongCode = parentLegalDongCode;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.legalDongName = legalDongName;
        this.fullAddressName = fullAddressName;
    }
}
