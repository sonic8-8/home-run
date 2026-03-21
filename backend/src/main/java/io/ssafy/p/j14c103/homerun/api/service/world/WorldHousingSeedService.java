package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.DocumentSeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.PropertySeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.WorldHousingSeedPlan;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorldHousingSeedService {

    private final WorldHousingSeedPolicy worldHousingSeedPolicy;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final RealEstateDocumentRepository realEstateDocumentRepository;

    public WorldHousingSeedService(
        final RealEstatePropertyRepository realEstatePropertyRepository,
        final RealEstateDocumentRepository realEstateDocumentRepository
    ) {
        this.worldHousingSeedPolicy = new WorldHousingSeedPolicy();
        this.realEstatePropertyRepository = realEstatePropertyRepository;
        this.realEstateDocumentRepository = realEstateDocumentRepository;
    }

    public void seed() {
        final WorldHousingSeedPlan seedPlan = worldHousingSeedPolicy.calculate();
        seedProperties(seedPlan);
        seedDocuments(seedPlan);
    }

    private void seedProperties(final WorldHousingSeedPlan seedPlan) {
        seedPlan.propertySeeds().forEach(this::seedProperty);
    }

    private void seedProperty(final PropertySeed propertySeed) {
        if (realEstatePropertyRepository.existsByProviderId(propertySeed.providerId())) {
            return;
        }

        realEstatePropertyRepository.save(
            RealEstateProperty.create(
                propertySeed.providerId(),
                propertySeed.name(),
                propertySeed.address(),
                propertySeed.regionCode(),
                propertySeed.districtCode(),
                propertySeed.price(),
                propertySeed.latitude(),
                propertySeed.longitude(),
                propertySeed.housingType(),
                propertySeed.contractTraps()
            )
        );
    }

    private void seedDocuments(final WorldHousingSeedPlan seedPlan) {
        seedPlan.documentSeeds().forEach(this::seedDocument);
    }

    private void seedDocument(final DocumentSeed documentSeed) {
        final RealEstateProperty property = realEstatePropertyRepository
            .findByProviderId(documentSeed.propertyProviderId())
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));

        if (realEstateDocumentRepository.existsByPropertyIdAndDocumentType(
            property.getPropertyId(),
            documentSeed.documentType()
        )) {
            return;
        }

        realEstateDocumentRepository.save(
            RealEstateDocument.create(
                property.getPropertyId(),
                documentSeed.documentType(),
                documentSeed.imageUrl(),
                documentSeed.checklist()
            )
        );
    }
}
