package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateDocumentRepository extends JpaRepository<RealEstateDocument, Long> {

    List<RealEstateDocument> findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(
        RealEstateRegistrySection registrySection
    );
}
