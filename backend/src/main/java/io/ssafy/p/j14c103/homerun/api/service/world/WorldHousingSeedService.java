package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.DistrictSeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.DocumentSeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.PropertySeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.RegionSeed;
import io.ssafy.p.j14c103.homerun.domain.world.housing.WorldHousingSeedPolicy.WorldHousingSeedPlan;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorldHousingSeedService {

    private static final RealEstateDocumentType REGISTRY_DOCUMENT_TYPE = RealEstateDocumentType.REGISTRY;
    private static final Map<String, RegionMasterSeed> REGION_MASTER_SEEDS = Map.of(
        "SEOUL", new RegionMasterSeed("11", "서울특별시"),
        "GWANGJU", new RegionMasterSeed("24", "광주광역시")
    );
    private static final Map<String, DistrictMasterSeed> DISTRICT_MASTER_SEEDS = Map.of(
        "GANGNAM", new DistrictMasterSeed("11", "11680", "강남구", "1168000000"),
        "SONGPA", new DistrictMasterSeed("11", "11710", "송파구", "1171000000"),
        "MAPO", new DistrictMasterSeed("11", "11440", "마포구", "1144000000"),
        "GWANGJIN", new DistrictMasterSeed("11", "11215", "광진구", "1121500000"),
        "BUKGU", new DistrictMasterSeed("24", "24170", "북구", "2417000000")
    );

    private final WorldHousingSeedPolicy worldHousingSeedPolicy;
    private final HousingRegionRepository housingRegionRepository;
    private final HousingDistrictRepository housingDistrictRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final RealEstateDocumentRepository realEstateDocumentRepository;

    public WorldHousingSeedService(
        final HousingRegionRepository housingRegionRepository,
        final HousingDistrictRepository housingDistrictRepository,
        final RealEstatePropertyRepository realEstatePropertyRepository,
        final RealEstateDocumentRepository realEstateDocumentRepository
    ) {
        this.worldHousingSeedPolicy = new WorldHousingSeedPolicy();
        this.housingRegionRepository = housingRegionRepository;
        this.housingDistrictRepository = housingDistrictRepository;
        this.realEstatePropertyRepository = realEstatePropertyRepository;
        this.realEstateDocumentRepository = realEstateDocumentRepository;
    }

    public void seed() {
        final WorldHousingSeedPlan seedPlan = worldHousingSeedPolicy.calculate();
        seedRegions(seedPlan);
        seedDistricts(seedPlan);
        seedProperties(seedPlan);
        seedDocuments(seedPlan);
    }

    private void seedRegions(final WorldHousingSeedPlan seedPlan) {
        seedPlan.regionSeeds().forEach(this::seedRegion);
    }

    private void seedRegion(final RegionSeed regionSeed) {
        final RegionMasterSeed masterSeed = toRegionMasterSeed(regionSeed.regionCode());

        housingRegionRepository.findById(masterSeed.regionCode())
            .ifPresentOrElse(
                region -> region.update(masterSeed.regionName()),
                () -> housingRegionRepository.save(
                    HousingRegion.create(masterSeed.regionCode(), masterSeed.regionName())
                )
            );
    }

    private void seedDistricts(final WorldHousingSeedPlan seedPlan) {
        seedPlan.districtSeeds().forEach(this::seedDistrict);
    }

    private void seedDistrict(final DistrictSeed districtSeed) {
        final DistrictMasterSeed masterSeed = toDistrictMasterSeed(districtSeed.districtCode());
        validateDistrictRegionMapping(districtSeed.regionCode(), masterSeed.regionCode());

        housingDistrictRepository.findById(masterSeed.districtCode())
            .ifPresentOrElse(
                district -> district.update(
                    masterSeed.regionCode(),
                    masterSeed.districtName(),
                    masterSeed.legalDongCode()
                ),
                () -> housingDistrictRepository.save(
                    HousingDistrict.create(
                        masterSeed.districtCode(),
                        masterSeed.regionCode(),
                        masterSeed.districtName(),
                        masterSeed.legalDongCode()
                    )
                )
            );
    }

    private void seedProperties(final WorldHousingSeedPlan seedPlan) {
        seedPlan.propertySeeds().forEach(this::seedProperty);
    }

    private void seedProperty(final PropertySeed propertySeed) {
        final DistrictMasterSeed districtMasterSeed = toDistrictMasterSeed(propertySeed.districtCode());
        validateDistrictRegionMapping(propertySeed.regionCode(), districtMasterSeed.regionCode());

        realEstatePropertyRepository.findByProviderId(propertySeed.providerId())
            .ifPresentOrElse(
                property -> property.updateFromImport(
                    propertySeed.name(),
                    propertySeed.address(),
                    districtMasterSeed.regionCode(),
                    districtMasterSeed.districtCode(),
                    districtMasterSeed.legalDongCode(),
                    propertySeed.price(),
                    propertySeed.latitude(),
                    propertySeed.longitude(),
                    null,
                    null,
                    propertySeed.housingType(),
                    propertySeed.contractTraps()
                ),
                () -> realEstatePropertyRepository.save(
                    RealEstateProperty.create(
                        propertySeed.providerId(),
                        propertySeed.name(),
                        propertySeed.address(),
                        districtMasterSeed.regionCode(),
                        districtMasterSeed.districtCode(),
                        districtMasterSeed.legalDongCode(),
                        propertySeed.price(),
                        propertySeed.latitude(),
                        propertySeed.longitude(),
                        null,
                        null,
                        propertySeed.housingType(),
                        propertySeed.contractTraps()
                    )
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

        if (realEstateDocumentRepository.existsByPropertyIdAndDocumentTypeAndRegistrySection(
            property.getPropertyId(),
            REGISTRY_DOCUMENT_TYPE,
            documentSeed.registrySection()
        )) {
            return;
        }

        realEstateDocumentRepository.save(
            RealEstateDocument.create(
                property.getPropertyId(),
                REGISTRY_DOCUMENT_TYPE,
                documentSeed.registrySection(),
                documentSeed.quizSamplePayload()
            )
        );
    }

    private RegionMasterSeed toRegionMasterSeed(final String regionSeedCode) {
        final RegionMasterSeed masterSeed = REGION_MASTER_SEEDS.get(regionSeedCode);
        if (masterSeed != null) {
            return masterSeed;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private DistrictMasterSeed toDistrictMasterSeed(final String districtSeedCode) {
        final DistrictMasterSeed masterSeed = DISTRICT_MASTER_SEEDS.get(districtSeedCode);
        if (masterSeed != null) {
            return masterSeed;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private void validateDistrictRegionMapping(
        final String regionSeedCode,
        final String masterRegionCode
    ) {
        if (toRegionMasterSeed(regionSeedCode).regionCode().equals(masterRegionCode)) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private record RegionMasterSeed(
        String regionCode,
        String regionName
    ) {
    }

    private record DistrictMasterSeed(
        String regionCode,
        String districtCode,
        String districtName,
        String legalDongCode
    ) {
    }
}
