package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousingLegalDongRepository extends JpaRepository<HousingLegalDong, String> {

    Optional<HousingLegalDong> findByDistrictCodeAndLegalDongName(
        String districtCode,
        String legalDongName
    );
}
