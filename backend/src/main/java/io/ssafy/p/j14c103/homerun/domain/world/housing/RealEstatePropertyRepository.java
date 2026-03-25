package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RealEstatePropertyRepository extends JpaRepository<RealEstateProperty, Long> {

    Optional<RealEstateProperty> findByProviderId(String providerId);

    @Query("select distinct property.regionCode from RealEstateProperty property")
    List<String> findDistinctRegionCodes();

    @Query("""
        select distinct property.districtCode
        from RealEstateProperty property
        where property.regionCode = :regionCode
        """)
    List<String> findDistinctDistrictCodesByRegionCode(@Param("regionCode") String regionCode);

    List<RealEstateProperty> findAllByRegionCodeAndDistrictCodeOrderByPropertyIdAsc(
        String regionCode,
        String districtCode
    );

    boolean existsByProviderId(String providerId);
}
