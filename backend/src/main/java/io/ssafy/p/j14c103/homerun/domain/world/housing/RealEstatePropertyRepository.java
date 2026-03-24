package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstatePropertyRepository extends JpaRepository<RealEstateProperty, Integer> {

    Optional<RealEstateProperty> findByProviderId(String providerId);

    List<RealEstateProperty> findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
        String regionCode,
        String districtCode
    );

    boolean existsByProviderId(String providerId);
}
