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
            "GAPGU",
            quizSampleDefinition(
                "위험",
                List.of(
                    registryRowDefinition(
                        "1",
                        "소유권이전청구권가등기",
                        "2025년 1월 8일",
                        "매매예약",
                        "소유권 이전 청구가등기가 남아 있어 실제 소유 관계를 다시 확인해야 한다",
                        Map.of()
                    )
                ),
                "갑구에 소유권 관련 선순위 권리 표시가 있어 주의가 필요하다.",
                List.of("실제 소유자와 계약 당사자 일치 여부 확인", "가등기 말소 여부 확인"),
                "소유권 관련 위험 신호를 확인했다.",
                "소유권 관련 권리 표시를 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-HN-001",
            "EULGU",
            quizSampleDefinition(
                "위험",
                List.of(
                    registryRowDefinition(
                        "1",
                        "근저당권설정",
                        "2025년 2월 7일",
                        "2025년 2월 1일 설정계약",
                        "채권최고액 {max_claim_amount} 근저당권이 설정되어 있다",
                        Map.of(
                            "max_claim_amount",
                            moneyRenderingDefinition("sale_price_ratio", 78, "만원")
                        )
                    )
                ),
                "을구의 근저당 채권최고액이 높아 대출 회수 위험이 크다.",
                List.of("채권최고액과 매매가 비율 확인", "선순위 담보권 존재 여부 확인"),
                "근저당 설정 위험을 확인했다.",
                "과도한 근저당 설정을 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-HN-002",
            "GAPGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "소유권보존",
                        "2024년 11월 3일",
                        "보존",
                        "소유권보존 등기 외 특이사항이 없다",
                        Map.of()
                    )
                ),
                "갑구에는 특이한 권리 침해 요소가 없다.",
                List.of("현재 소유자 확인", "갑구 특이사항 없음"),
                "갑구를 정상적으로 확인했다.",
                "갑구의 기본 소유권 정보를 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-HN-002",
            "EULGU",
            quizSampleDefinition(
                "위험",
                List.of(
                    registryRowDefinition(
                        "1",
                        "근저당권설정",
                        "2025년 2월 14일",
                        "2025년 2월 10일 설정계약",
                        "채권최고액 {max_claim_amount} 근저당권이 설정되어 있다",
                        Map.of(
                            "max_claim_amount",
                            moneyRenderingDefinition("sale_price_ratio", 92, "만원")
                        )
                    )
                ),
                "을구의 담보 설정 규모가 커 보증금 회수 위험이 높다.",
                List.of("보증금 대비 선순위 권리 규모 확인", "추가 담보 설정 가능성 점검"),
                "보증금 회수 위험을 확인했다.",
                "보증금 회수 위험 신호를 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-SP-001",
            "GAPGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "소유권보존",
                        "2024년 9월 12일",
                        "보존",
                        "갑구에 기재된 권리 사항이 안정적이다",
                        Map.of()
                    )
                ),
                "갑구에서 별도 위험 신호가 확인되지 않았다.",
                List.of("소유권 변동 이력 없음", "갑구 특이사항 없음"),
                "갑구를 정상적으로 확인했다.",
                "갑구의 정상 상태를 확인하지 못했다."
            )
        ),
        documentDefinition(
            "PROP-SP-001",
            "EULGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "기록사항 없음",
                        "2025년 1월 20일",
                        "없음",
                        "을구에 현재 등재된 권리 사항이 없다",
                        Map.of()
                    )
                ),
                "을구에서 추가 담보권이나 압류가 확인되지 않았다.",
                List.of("을구 권리 사항 없음", "담보권 미설정"),
                "을구를 정상적으로 확인했다.",
                "을구의 정상 상태를 확인하지 못했다."
            )
        ),
        documentDefinition(
            "PROP-MP-001",
            "GAPGU",
            quizSampleDefinition(
                "위험",
                List.of(
                    registryRowDefinition(
                        "1",
                        "위반건축물 표기",
                        "2025년 2월 3일",
                        "행정통보",
                        "불법 증축 이력이 기재되어 있어 건축물 현황을 추가 확인해야 한다",
                        Map.of()
                    )
                ),
                "갑구에 건축물 관련 위험 신호가 있어 주의가 필요하다.",
                List.of("건축물대장 일치 여부 확인", "위반건축물 시정 여부 확인"),
                "건축물 관련 위험 신호를 확인했다.",
                "건축물 관련 위험 신호를 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-MP-001",
            "EULGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "기록사항 없음",
                        "2025년 2월 3일",
                        "없음",
                        "을구에 등재된 권리 사항이 없다",
                        Map.of()
                    )
                ),
                "을구에서는 별도 담보 위험이 확인되지 않았다.",
                List.of("을구 권리 사항 없음", "담보권 미설정"),
                "을구를 정상적으로 확인했다.",
                "을구의 정상 상태를 확인하지 못했다."
            )
        ),
        documentDefinition(
            "PROP-GJN-001",
            "GAPGU",
            quizSampleDefinition(
                "위험",
                List.of(
                    registryRowDefinition(
                        "1",
                        "압류",
                        "2025년 1월 27일",
                        "지방세 체납",
                        "지방세 체납으로 인한 압류가 기재되어 있다",
                        Map.of()
                    )
                ),
                "갑구에 체납 압류 이력이 있어 매수 시 위험하다.",
                List.of("세금 체납 정리 여부 확인", "압류 말소 가능 여부 확인"),
                "압류 이력을 확인했다.",
                "압류 이력을 놓쳤다."
            )
        ),
        documentDefinition(
            "PROP-GJN-001",
            "EULGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "기록사항 없음",
                        "2025년 1월 27일",
                        "없음",
                        "을구에 추가 담보권 설정이 없다",
                        Map.of()
                    )
                ),
                "을구에서는 추가 담보 위험이 보이지 않는다.",
                List.of("을구 권리 사항 없음", "추가 담보권 미설정"),
                "을구를 정상적으로 확인했다.",
                "을구의 정상 상태를 확인하지 못했다."
            )
        ),
        documentDefinition(
            "PROP-GWJ-001",
            "GAPGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "소유권보존",
                        "2024년 8월 21일",
                        "보존",
                        "갑구에 특이사항 없이 소유권보존만 기재되어 있다",
                        Map.of()
                    )
                ),
                "갑구에서 별도 권리 침해 요소가 보이지 않는다.",
                List.of("소유권 상태 정상", "갑구 특이사항 없음"),
                "갑구를 정상적으로 확인했다.",
                "갑구의 정상 상태를 확인하지 못했다."
            )
        ),
        documentDefinition(
            "PROP-GWJ-001",
            "EULGU",
            quizSampleDefinition(
                "정상",
                List.of(
                    registryRowDefinition(
                        "1",
                        "기록사항 없음",
                        "2024년 8월 21일",
                        "없음",
                        "을구에 현재 등재된 권리 사항이 없다",
                        Map.of()
                    )
                ),
                "을구에서 담보권이나 압류가 확인되지 않는다.",
                List.of("을구 권리 사항 없음", "담보권 미설정"),
                "을구를 정상적으로 확인했다.",
                "을구의 정상 상태를 확인하지 못했다."
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
                RealEstateRegistrySection.valueOf(requiredText(definition, "registrySection")),
                toQuizSample(requiredMap(definition, "quizSamplePayload"))
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

    private RealEstateRegistryQuizSample toQuizSample(final Map<String, Object> definition) {
        return RealEstateRegistryQuizSample.create(
            requiredText(definition, "quizVerdict"),
            toRegistryRows(requiredList(definition, "rows")),
            requiredText(definition, "issueSummary"),
            toTexts(requiredList(definition, "keyPoints")),
            requiredText(definition, "feedbackCorrect"),
            requiredText(definition, "feedbackWrong")
        );
    }

    private List<RealEstateRegistryRow> toRegistryRows(final List<?> definitions) {
        return definitions.stream()
            .map(this::toRegistryRow)
            .toList();
    }

    private RealEstateRegistryRow toRegistryRow(final Object definition) {
        if (!(definition instanceof Map<?, ?> rawDefinition)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> rowDefinition = (Map<String, Object>) rawDefinition;
        return RealEstateRegistryRow.create(
            requiredText(rowDefinition, "rankNo"),
            requiredText(rowDefinition, "purpose"),
            requiredText(rowDefinition, "receipt"),
            requiredText(rowDefinition, "reason"),
            requiredText(rowDefinition, "details"),
            toRenderingRules(optionalMap(rowDefinition, "rendering"))
        );
    }

    private List<String> toTexts(final List<?> definitions) {
        return definitions.stream()
            .map(this::toText)
            .toList();
    }

    private String toText(final Object definition) {
        if (definition instanceof String text && !text.isBlank()) {
            return text;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private Map<String, RealEstateMoneyRenderingRule> toRenderingRules(final Map<String, Object> definition) {
        if (definition.isEmpty()) {
            return Map.of();
        }

        return definition.entrySet().stream()
            .collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> toMoneyRenderingRule(entry.getValue())
                )
            );
    }

    private RealEstateMoneyRenderingRule toMoneyRenderingRule(final Object definition) {
        if (!(definition instanceof Map<?, ?> rawDefinition)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> ruleDefinition = (Map<String, Object>) rawDefinition;
        return RealEstateMoneyRenderingRule.create(
            requiredText(ruleDefinition, "source"),
            requiredInt(ruleDefinition, "ratioPercent"),
            requiredText(ruleDefinition, "roundingUnit")
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

    private Map<String, Object> optionalMap(final Map<String, Object> definition, final String key) {
        final Object value = definition.get(key);
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> typedMap = (Map<String, Object>) rawMap;
            return typedMap;
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
        final String registrySection,
        final Map<String, Object> quizSamplePayload
    ) {
        return Map.of(
            "propertyProviderId", propertyProviderId,
            "registrySection", registrySection,
            "quizSamplePayload", quizSamplePayload
        );
    }

    private static Map<String, Object> quizSampleDefinition(
        final String quizVerdict,
        final List<Map<String, Object>> rows,
        final String issueSummary,
        final List<String> keyPoints,
        final String feedbackCorrect,
        final String feedbackWrong
    ) {
        return Map.of(
            "quizVerdict", quizVerdict,
            "rows", rows,
            "issueSummary", issueSummary,
            "keyPoints", keyPoints,
            "feedbackCorrect", feedbackCorrect,
            "feedbackWrong", feedbackWrong
        );
    }

    private static Map<String, Object> registryRowDefinition(
        final String rankNo,
        final String purpose,
        final String receipt,
        final String reason,
        final String details,
        final Map<String, Object> rendering
    ) {
        return Map.of(
            "rankNo", rankNo,
            "purpose", purpose,
            "receipt", receipt,
            "reason", reason,
            "details", details,
            "rendering", rendering
        );
    }

    private static Map<String, Object> moneyRenderingDefinition(
        final String source,
        final int ratioPercent,
        final String roundingUnit
    ) {
        return Map.of(
            "source", source,
            "ratioPercent", ratioPercent,
            "roundingUnit", roundingUnit
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
        RealEstateRegistrySection registrySection,
        RealEstateRegistryQuizSample quizSamplePayload
    ) {
    }
}
