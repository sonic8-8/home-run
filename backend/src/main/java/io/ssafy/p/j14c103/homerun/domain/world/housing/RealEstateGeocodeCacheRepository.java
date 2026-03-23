package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateGeocodeCacheRepository extends JpaRepository<RealEstateGeocodeCache, Long> {

    Optional<RealEstateGeocodeCache> findByGeocodingQuery(String geocodingQuery);
}
