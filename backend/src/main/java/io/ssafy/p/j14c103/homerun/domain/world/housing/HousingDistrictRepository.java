package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousingDistrictRepository extends JpaRepository<HousingDistrict, String> {

    List<HousingDistrict> findAllByRegionCodeOrderByDistrictCodeAsc(String regionCode);
}
