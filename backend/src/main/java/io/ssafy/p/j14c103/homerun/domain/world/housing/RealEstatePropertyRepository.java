package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstatePropertyRepository extends JpaRepository<RealEstateProperty, Long> {

    List<RealEstateProperty> findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
        String regionCode,
        String districtCode
    );
}
