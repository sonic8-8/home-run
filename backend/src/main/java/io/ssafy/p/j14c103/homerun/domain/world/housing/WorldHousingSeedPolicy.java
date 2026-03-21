package io.ssafy.p.j14c103.homerun.domain.world.housing;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorldHousingSeedPolicy {

    private static final String SEOUL = "SEOUL";
    private static final String GWANGJU = "GWANGJU";

    private static final List<Map<String, Object>> REGION_DEFINITIONS = List.of(
        regionDefinition(SEOUL, "서울"),
        regionDefinition(GWANGJU, "광주")
    );

    private static final List<Map<String, Object>> DISTRICT_DEFINITIONS = List.of(
        districtDefinition(SEOUL, "GANGNAM", "강남구"),
        districtDefinition(SEOUL, "SONGPA", "송파구"),
        districtDefinition(SEOUL, "MAPO", "마포구"),
        districtDefinition(SEOUL, "GWANGJIN", "광진구"),
        districtDefinition(GWANGJU, "BUKGU", "북구")
    );

    private static final List<Map<String, Object>> PROPERTY_DEFINITIONS = List.of(
        propertyDefinition(
            "PROP-HN-001",
            "하남3지구 모아엘가 더 퍼스트",
            "서울시 강남구 테헤란로 101",
            SEOUL,
            "GANGNAM",
            375_000_000L,
            "37.5172",
            "127.0473",
            "OWNED_APT",
            List.of(
                trapDefinition(
                    "TRAP-HN-001",
                    "HIGH_MORTGAGE",
                    "REGISTRY",
                    "과도한 근저당이 설정된 매물",
                    penaltyDefinition("-5000000", 20)
                ),
                trapDefinition(
                    "TRAP-HN-002",
                    "OWNER_MISMATCH",
                    "CONTRACT",
                    "계약서와 소유자 정보가 일치하지 않음",
                    penaltyDefinition("-10000000", 30)
                )
            )
        ),
        propertyDefinition(
            "PROP-HN-002",
            "하남 포레스트 힐",
            "서울시 강남구 영동대로 201",
            SEOUL,
            "GANGNAM",
            420_000_000L,
            "37.5200",
            "127.0500",
            "OWNED_APT",
            List.of(
                trapDefinition(
                    "TRAP-HN-003",
                    "LEASE_SCAM",
                    "REGISTRY",
                    "보증금 회수 위험이 있는 매물",
                    penaltyDefinition("ALL_DEPOSIT_LOST", 40)
                )
            )
        ),
        propertyDefinition(
            "PROP-SP-001",
            "송파 센트럴 파크",
            "서울시 송파구 올림픽로 300",
            SEOUL,
            "SONGPA",
            360_000_000L,
            "37.5145",
            "127.1062",
            "JEONSE_APT",
            List.of(
                trapDefinition(
                    "TRAP-SP-001",
                    "NOTICE_DEFECT",
                    "CONTRACT",
                    "특약 누락으로 분쟁 위험이 있음",
                    penaltyDefinition("-3000000", 10)
                )
            )
        ),
        propertyDefinition(
            "PROP-MP-001",
            "마포 리버뷰 빌라",
            "서울시 마포구 독막로 88",
            SEOUL,
            "MAPO",
            285_000_000L,
            "37.5481",
            "126.9058",
            "VILLA",
            List.of(
                trapDefinition(
                    "TRAP-MP-001",
                    "ILLEGAL_EXTENSION",
                    "REGISTRY",
                    "불법 증축 이력이 확인된 매물",
                    penaltyDefinition("-2000000", 8)
                )
            )
        ),
        propertyDefinition(
            "PROP-GJN-001",
            "광진 자이 타워",
            "서울시 광진구 아차산로 50",
            SEOUL,
            "GWANGJIN",
            340_000_000L,
            "37.5384",
            "127.0822",
            "JEONSE_APT",
            List.of(
                trapDefinition(
                    "TRAP-GJN-001",
                    "UNPAID_TAX",
                    "REGISTRY",
                    "체납 이력이 있는 매물",
                    penaltyDefinition("-1500000", 12)
                )
            )
        ),
        propertyDefinition(
            "PROP-GWJ-001",
            "광주 첨단 드림하우스",
            "광주광역시 북구 첨단과기로 123",
            GWANGJU,
            "BUKGU",
            190_000_000L,
            "35.1763",
            "126.9111",
            "STUDIO",
            List.of(
                trapDefinition(
                    "TRAP-GWJ-001",
                    "MAINTENANCE_ARREARS",
                    "CONTRACT",
                    "관리비 체납 이력이 있는 매물",
                    penaltyDefinition("-1000000", 6)
                )
            )
        )
    );

    private static final List<Map<String, Object>> DOCUMENT_DEFINITIONS = List.of(
        documentDefinition(
            "PROP-HN-001",
            "REGISTRY",
            "/images/docs/prop-hn-001-registry.png",
            List.of(
                checklistItemDefinition("TRAP-HN-001", "근저당 설정 여부 확인", true),
                checklistItemDefinition("TRAP-HN-002", "소유자 정보 일치 여부 확인", false)
            )
        ),
        documentDefinition(
            "PROP-HN-001",
            "CONTRACT",
            "/images/docs/prop-hn-001-contract.png",
            List.of(
                checklistItemDefinition("TRAP-HN-002", "계약 당사자와 소유자 정보 확인", true)
            )
        ),
        documentDefinition(
            "PROP-HN-002",
            "REGISTRY",
            "/images/docs/prop-hn-002-registry.png",
            List.of(
                checklistItemDefinition("TRAP-HN-003", "보증금 회수 위험 확인", true)
            )
        ),
        documentDefinition(
            "PROP-SP-001",
            "CONTRACT",
            "/images/docs/prop-sp-001-contract.png",
            List.of(
                checklistItemDefinition("TRAP-SP-001", "특약 조항 누락 여부 확인", true)
            )
        ),
        documentDefinition(
            "PROP-MP-001",
            "REGISTRY",
            "/images/docs/prop-mp-001-registry.png",
            List.of(
                checklistItemDefinition("TRAP-MP-001", "불법 증축 여부 확인", true)
            )
        ),
        documentDefinition(
            "PROP-GJN-001",
            "REGISTRY",
            "/images/docs/prop-gjn-001-registry.png",
            List.of(
                checklistItemDefinition("TRAP-GJN-001", "체납 이력 확인", true)
            )
        ),
        documentDefinition(
            "PROP-GWJ-001",
            "CONTRACT",
            "/images/docs/prop-gwj-001-contract.png",
            List.of(
                checklistItemDefinition("TRAP-GWJ-001", "관리비 체납 여부 확인", true)
            )
        )
    );

    public WorldHousingSeedPlan calculate() {
        final List<RegionSeed> regionSeeds = createRegionSeeds(REGION_DEFINITIONS);
        final List<DistrictSeed> districtSeeds = createDistrictSeeds(regionSeeds, DISTRICT_DEFINITIONS);
        final List<PropertySeed> propertySeeds = createPropertySeeds(districtSeeds, PROPERTY_DEFINITIONS);
        final List<DocumentSeed> documentSeeds = createDocumentSeeds(propertySeeds, DOCUMENT_DEFINITIONS);

        return new WorldHousingSeedPlan(regionSeeds, districtSeeds, propertySeeds, documentSeeds);
    }

    public List<RegionSeed> createRegionSeeds(final List<Map<String, Object>> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return definitions.stream()
            .map(this::toRegionSeed)
            .toList();
    }

    public List<DistrictSeed> createDistrictSeeds(
        final List<RegionSeed> regionSeeds,
        final List<Map<String, Object>> definitions
    ) {
        if (regionSeeds == null || regionSeeds.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        final Set<String> regionCodes = regionSeeds.stream()
            .map(RegionSeed::regionCode)
            .collect(java.util.stream.Collectors.toSet());

        return definitions.stream()
            .map(definition -> toDistrictSeed(regionCodes, definition))
            .toList();
    }

    public List<PropertySeed> createPropertySeeds(final List<Map<String, Object>> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return definitions.stream()
            .map(this::toPropertySeed)
            .toList();
    }

    public List<DocumentSeed> createDocumentSeeds(
        final List<PropertySeed> propertySeeds,
        final List<Map<String, Object>> definitions
    ) {
        if (propertySeeds == null || propertySeeds.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (definitions == null || definitions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        final Set<String> propertyProviderIds = propertySeeds.stream()
            .map(PropertySeed::providerId)
            .collect(java.util.stream.Collectors.toSet());

        return definitions.stream()
            .map(definition -> toDocumentSeed(propertyProviderIds, definition))
            .toList();
    }

    private List<PropertySeed> createPropertySeeds(
        final List<DistrictSeed> districtSeeds,
        final List<Map<String, Object>> definitions
    ) {
        final List<PropertySeed> propertySeeds = createPropertySeeds(definitions);
        final Set<String> districtKeys = districtSeeds.stream()
            .map(districtSeed -> districtSeed.regionCode() + ":" + districtSeed.districtCode())
            .collect(java.util.stream.Collectors.toSet());

        propertySeeds.forEach(propertySeed -> validateDistrictExists(districtKeys, propertySeed));
        return propertySeeds;
    }

    private RegionSeed toRegionSeed(final Map<String, Object> definition) {
        return new RegionSeed(
            requiredText(definition, "regionCode"),
            requiredText(definition, "name")
        );
    }

    private DistrictSeed toDistrictSeed(
        final Set<String> regionCodes,
        final Map<String, Object> definition
    ) {
        final String regionCode = requiredText(definition, "regionCode");
        if (!regionCodes.contains(regionCode)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return new DistrictSeed(
            regionCode,
            requiredText(definition, "districtCode"),
            requiredText(definition, "name")
        );
    }

    private PropertySeed toPropertySeed(final Map<String, Object> definition) {
        try {
            return new PropertySeed(
                requiredText(definition, "providerId"),
                requiredText(definition, "name"),
                requiredText(definition, "address"),
                requiredText(definition, "regionCode"),
                requiredText(definition, "districtCode"),
                Money.of(requiredLong(definition, "price")),
                requiredBigDecimal(definition, "latitude"),
                requiredBigDecimal(definition, "longitude"),
                HousingType.valueOf(requiredText(definition, "housingType")),
                toContractTraps(optionalList(definition, "contractTraps"))
            );
        } catch (IllegalArgumentException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
        }
    }

    private DocumentSeed toDocumentSeed(
        final Set<String> propertyProviderIds,
        final Map<String, Object> definition
    ) {
        final String propertyProviderId = requiredText(definition, "propertyProviderId");
        if (!propertyProviderIds.contains(propertyProviderId)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        try {
            return new DocumentSeed(
                propertyProviderId,
                RealEstateDocumentType.valueOf(requiredText(definition, "documentType")),
                requiredText(definition, "imageUrl"),
                toChecklist(requiredList(definition, "checklist"))
            );
        } catch (IllegalArgumentException exception) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
        }
    }

    private List<ContractTrap> toContractTraps(final List<?> definitions) {
        return definitions.stream()
            .map(this::toContractTrap)
            .toList();
    }

    private ContractTrap toContractTrap(final Object definition) {
        if (!(definition instanceof Map<?, ?> rawDefinition)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> trapDefinition = (Map<String, Object>) rawDefinition;
        return ContractTrap.create(
            requiredText(trapDefinition, "trapId"),
            requiredText(trapDefinition, "type"),
            requiredText(trapDefinition, "documentType"),
            requiredText(trapDefinition, "description"),
            toPenalty(requiredMap(trapDefinition, "penalty"))
        );
    }

    private ContractTrapPenalty toPenalty(final Map<String, Object> definition) {
        return ContractTrapPenalty.create(
            requiredText(definition, "cash"),
            requiredInt(definition, "stress")
        );
    }

    private List<RealEstateChecklistItem> toChecklist(final List<?> definitions) {
        return definitions.stream()
            .map(this::toChecklistItem)
            .toList();
    }

    private RealEstateChecklistItem toChecklistItem(final Object definition) {
        if (!(definition instanceof Map<?, ?> rawDefinition)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> checklistDefinition = (Map<String, Object>) rawDefinition;
        return RealEstateChecklistItem.create(
            requiredText(checklistDefinition, "trapId"),
            requiredText(checklistDefinition, "label"),
            requiredBoolean(checklistDefinition, "isTrapped")
        );
    }

    private void validateDistrictExists(
        final Set<String> districtKeys,
        final PropertySeed propertySeed
    ) {
        final String districtKey = propertySeed.regionCode() + ":" + propertySeed.districtCode();
        if (districtKeys.contains(districtKey)) {
            return;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private String requiredText(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private Map<String, Object> requiredMap(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> typedMap = (Map<String, Object>) rawMap;
            return typedMap;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<?> requiredList(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof List<?> list && !list.isEmpty()) {
            return list;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<?> optionalList(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private long requiredLong(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException exception) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
            }
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private int requiredInt(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException exception) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
            }
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private BigDecimal requiredBigDecimal(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException exception) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
            }
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private Boolean requiredBoolean(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private static Map<String, Object> regionDefinition(
        final String regionCode,
        final String name
    ) {
        return Map.of(
            "regionCode", regionCode,
            "name", name
        );
    }

    private static Map<String, Object> districtDefinition(
        final String regionCode,
        final String districtCode,
        final String name
    ) {
        return Map.of(
            "regionCode", regionCode,
            "districtCode", districtCode,
            "name", name
        );
    }

    private static Map<String, Object> propertyDefinition(
        final String providerId,
        final String name,
        final String address,
        final String regionCode,
        final String districtCode,
        final long price,
        final String latitude,
        final String longitude,
        final String housingType,
        final List<Map<String, Object>> contractTraps
    ) {
        return Map.of(
            "providerId", providerId,
            "name", name,
            "address", address,
            "regionCode", regionCode,
            "districtCode", districtCode,
            "price", price,
            "latitude", latitude,
            "longitude", longitude,
            "housingType", housingType,
            "contractTraps", contractTraps
        );
    }

    private static Map<String, Object> trapDefinition(
        final String trapId,
        final String type,
        final String documentType,
        final String description,
        final Map<String, Object> penalty
    ) {
        return Map.of(
            "trapId", trapId,
            "type", type,
            "documentType", documentType,
            "description", description,
            "penalty", penalty
        );
    }

    private static Map<String, Object> penaltyDefinition(
        final String cash,
        final int stress
    ) {
        return Map.of(
            "cash", cash,
            "stress", stress
        );
    }

    private static Map<String, Object> documentDefinition(
        final String propertyProviderId,
        final String documentType,
        final String imageUrl,
        final List<Map<String, Object>> checklist
    ) {
        return Map.of(
            "propertyProviderId", propertyProviderId,
            "documentType", documentType,
            "imageUrl", imageUrl,
            "checklist", checklist
        );
    }

    private static Map<String, Object> checklistItemDefinition(
        final String trapId,
        final String label,
        final boolean isTrapped
    ) {
        return Map.of(
            "trapId", trapId,
            "label", label,
            "isTrapped", isTrapped
        );
    }

    public record WorldHousingSeedPlan(
        List<RegionSeed> regionSeeds,
        List<DistrictSeed> districtSeeds,
        List<PropertySeed> propertySeeds,
        List<DocumentSeed> documentSeeds
    ) {
    }

    public record RegionSeed(
        String regionCode,
        String name
    ) {
    }

    public record DistrictSeed(
        String regionCode,
        String districtCode,
        String name
    ) {
    }

    public record PropertySeed(
        String providerId,
        String name,
        String address,
        String regionCode,
        String districtCode,
        Money price,
        BigDecimal latitude,
        BigDecimal longitude,
        HousingType housingType,
        List<ContractTrap> contractTraps
    ) {
    }

    public record DocumentSeed(
        String propertyProviderId,
        RealEstateDocumentType documentType,
        String imageUrl,
        List<RealEstateChecklistItem> checklist
    ) {
    }
}
