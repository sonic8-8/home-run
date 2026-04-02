package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateDocumentRepository extends JpaRepository<RealEstateDocument, Long> {

    List<RealEstateDocument> findAllByPropertyIdOrderByRealEstateDocumentIdAsc(Long propertyId);

    List<RealEstateDocument> findAllByPropertyIdAndDocumentTypeAndRegistrySectionOrderByRealEstateDocumentIdAsc(
        Long propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection
    );

    List<RealEstateDocument> findAllByPropertyIdIsNullAndDocumentTypeAndRegistrySectionOrderByRealEstateDocumentIdAsc(
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection
    );

    boolean existsByPropertyIdAndDocumentTypeAndRegistrySection(
        Long propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection
    );

    List<RealEstateDocument> findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(
        RealEstateRegistrySection registrySection
    );
}
