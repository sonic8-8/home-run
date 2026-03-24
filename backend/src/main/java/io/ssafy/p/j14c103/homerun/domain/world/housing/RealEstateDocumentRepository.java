package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateDocumentRepository extends JpaRepository<RealEstateDocument, Long> {

    List<RealEstateDocument> findAllByPropertyIdOrderByRealEstateDocumentIdAsc(Integer propertyId);

    List<RealEstateDocument> findAllByPropertyIdAndDocumentTypeAndRegistrySectionOrderByRealEstateDocumentIdAsc(
        Integer propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection
    );

    boolean existsByPropertyIdAndDocumentType(Integer propertyId, RealEstateDocumentType documentType);

    boolean existsByPropertyIdAndDocumentTypeAndRegistrySection(
        Integer propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection
    );
}
