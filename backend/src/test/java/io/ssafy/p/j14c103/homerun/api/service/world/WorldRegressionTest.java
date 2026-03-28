package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnStateService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.WorldEndingHistoryProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.WorldForeclosureSignalProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.request.WorldForeclosureSignalProviderRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldForeclosureSignalProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldContractReviewSubmitService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldPurchaseValidationService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldRealEstateDocumentQueryService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldRealEstatePropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.history.HousingGameplayHistoryWriter;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldContractReviewSubmitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldPurchaseValidationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstateDocumentsQueryResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldPurchaseValidationServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnWorldStateResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertyValidationResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrap;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrapPenalty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentChecklistItem;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryQuizSample;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryRow;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistrySection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorldRegressionTest {

    @Autowired
    private WorldHousingSeedService worldHousingSeedService;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private TargetPropertyValidationService targetPropertyValidationService;

    @Autowired
    private GameTurnStateService gameTurnStateService;

    @Autowired
    private GameTurnWorldStateService gameTurnWorldStateService;

    @Autowired
    private WorldEventTriggerService worldEventTriggerService;

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldPendingEventProviderService worldPendingEventProviderService;

    @Autowired
    private WorldEventResolveExecutionService worldEventResolveExecutionService;

    @Autowired
    private WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;

    @Autowired
    private WorldRealEstateDocumentQueryService worldRealEstateDocumentQueryService;

    @Autowired
    private WorldContractReviewSubmitService worldContractReviewSubmitService;

    @Autowired
    private WorldPurchaseValidationService worldPurchaseValidationService;

    @Autowired
    private WorldEndingHistoryProviderService worldEndingHistoryProviderService;

    @Autowired
    private WorldForeclosureSignalProviderService worldForeclosureSignalProviderService;

    @Autowired
    private HousingGameplayHistoryWriter housingGameplayHistoryWriter;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("목표 매물 검증부터 turn, pending resolve, ending history까지 대표 성공 흐름을 한 번에 검증한다")
    @Test
    void worldSuccessFlow() {
        // given
        worldHousingSeedService.seed();
        worldContentSeedService.seed();
        final User user = saveUser("world-regression-success@example.com");
        final RealEstateProperty targetProperty = realEstatePropertyRepository.findByProviderId("PROP-HN-001")
            .orElseThrow();
        final TargetPropertyValidationResponse targetPropertyResponse =
            targetPropertyValidationService.validateTargetProperty(
                "11",
                "11680",
                targetProperty.getPropertyId()
            );
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createWorldSession(user.getId(), 12, CyclePhase.BOOM, targetProperty.getPropertyId())
        );
        final Integer gameId = Math.toIntExact(gameSession.getGameSessionId());
        gameStatRepository.saveAndFlush(GameStat.create(gameId, 70, 10, 10, 50, 70, 12));
        gameCareerRepository.saveAndFlush(createGameCareer(gameId, 12));

        final List<io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult.EventCandidate> eventCandidates =
            worldEventTriggerService.calculateEventCandidates(
                gameSession.getGameSessionId(),
                Map.of(
                    "EVT-VOICE-001", new BigDecimal("0.0100"),
                    "EVT-FAMILY-001", new BigDecimal("0.0200"),
                    "EVT-OVERTIME-001", new BigDecimal("0.1500")
                )
            );
        worldPendingEventQueueService.enqueuePendingEvents(gameSession.getGameSessionId(), eventCandidates);

        final PendingEventsProviderResponse pendingEvents =
            worldPendingEventProviderService.getPendingEvents(gameSession.getGameSessionId());
        final PendingEventsProviderResponse.PendingEventItem familyEvent =
            findEventByTitle(pendingEvents, "경조사");
        final PendingEventsProviderResponse.PendingEventChoiceItem attendChoice =
            findChoiceByCode(familyEvent, "A");

        gameHousingRepository.saveAndFlush(
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.VILLA,
                Money.of(20_000_000L),
                Money.zero(),
                Money.of(120_000L),
                202L
            )
        );
        housingGameplayHistoryWriter.writeHousingChange(
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.STUDIO,
                Money.of(10_000_000L),
                Money.of(500_000L),
                Money.of(80_000L),
                201L
            ),
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.VILLA,
                Money.of(20_000_000L),
                Money.zero(),
                Money.of(120_000L),
                202L
            ),
            11,
            "빌라로 이사했다."
        );

        // when
        final TurnStateResponse turnResponse = gameTurnStateService.getTurnState(
            gameSession.getUserId(),
            gameSession.getGameSessionId()
        );
        final GameTurnWorldStateResponse turnWorldState = gameTurnWorldStateService.getWorldState(
            gameSession
        );
        worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            familyEvent.getEventId(),
            attendChoice.getChoiceId()
        );
        final WorldEndingHistoryProviderResponse endingHistory =
            worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId());

        // then
        assertThat(targetPropertyResponse.getPropertyId()).isEqualTo(targetProperty.getPropertyId());
        assertThat(targetPropertyResponse.getHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(turnResponse.getTurnNumber()).isEqualTo(12);
        assertThat(turnWorldState.getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(familyEvent.getType()).isEqualTo(EventPresentationType.CHOICE);
        assertThat(endingHistory.getEventHistories()).hasSize(1);
        assertThat(endingHistory.getEventHistories().get(0).getSelectedChoiceCode()).isEqualTo("A");
        assertThat(endingHistory.getHousingHistories()).hasSize(1);
        assertThat(endingHistory.getHousingHistories().get(0).getAfterState().getHousingType())
            .isEqualTo(HousingType.VILLA);
        assertThat(endingHistory.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(202L);
        assertThat(endingHistory.getHousingSnapshot().getTargetPropertyId()).isEqualTo(targetProperty.getPropertyId());
    }

    @DisplayName("부동산 계약 검토와 구매 검증 성공 후 엔딩 history가 현재 주거와 이동 이력을 함께 제공한다")
    @Test
    void realEstateAndEndingFlow() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty(
            "PROP-REG-SUCCESS",
            "회귀 테스트 성공 매물",
            BigDecimal.valueOf(37.5000),
            BigDecimal.valueOf(127.0300),
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "-1000000", 10)
            )
        ));
        realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            "/images/docs/contract-regression-success.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-01", "가압류 확인", true),
                RealEstateDocumentChecklistItem.create("TRAP-02", "근저당 확인", true)
            )
        ));
        final User user = saveUser("world-regression-real-estate@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createSimpleSession(user.getId(), property.getPropertyId())
        );
        final GameHousing beforeHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );
        gameHousingRepository.saveAndFlush(beforeHousing);

        // when
        final RealEstateDocumentsQueryResponse documentsResponse =
            worldRealEstateDocumentQueryService.getDocuments(property.getPropertyId());
        final WorldContractReviewSubmitServiceResponse reviewResponse =
            worldContractReviewSubmitService.submit(
                WorldContractReviewSubmitServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId(),
                    List.of("TRAP-01", "TRAP-02")
                )
            );
        final WorldPurchaseValidationServiceResponse validationResponse =
            worldPurchaseValidationService.validate(
                WorldPurchaseValidationServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId()
                )
            );
        final GameHousing afterHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.OWNED_APT,
            Money.of(375_000_000L),
            Money.zero(),
            Money.of(150_000L),
            property.getPropertyId()
        );
        gameHousingRepository.saveAndFlush(afterHousing);
        housingGameplayHistoryWriter.writeHousingChange(
            beforeHousing,
            afterHousing,
            12,
            "목표 매물을 매수했다."
        );
        final WorldEndingHistoryProviderResponse endingHistory =
            worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId());

        // then
        assertThat(documentsResponse.getDocuments()).hasSize(1);
        assertThat(reviewResponse.getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(validationResponse.isPassed()).isTrue();
        assertThat(validationResponse.getPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(endingHistory.getHousingHistories()).hasSize(1);
        assertThat(endingHistory.getHousingHistories().get(0).getAfterState().getPropertyId())
            .isEqualTo(property.getPropertyId());
        assertThat(endingHistory.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(endingHistory.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(property.getPropertyId());
    }

    @DisplayName("critical trap 미탐지는 구매 검증 실패로 이어지고 foreclosure signal 대표 케이스를 함께 검증한다")
    @Test
    void failureAndSignalFlow() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty(
            "PROP-REG-FAIL",
            "회귀 테스트 실패 매물",
            BigDecimal.valueOf(37.5100),
            BigDecimal.valueOf(127.0400),
            List.of(
                createTrap("TRAP-CRITICAL", "ALL_DEPOSIT_LOST", 50),
                createTrap("TRAP-NORMAL", "-1000000", 10)
            )
        ));
        realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            "/images/docs/contract-regression-fail.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-CRITICAL", "보증금 전액 손실 위험 확인", true),
                RealEstateDocumentChecklistItem.create("TRAP-NORMAL", "근저당 확인", true)
            )
        ));
        final User user = saveUser("world-regression-signal@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createSimpleSession(user.getId(), property.getPropertyId())
        );

        // when
        final WorldContractReviewSubmitServiceResponse reviewResponse =
            worldContractReviewSubmitService.submit(
                WorldContractReviewSubmitServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId(),
                    List.of("TRAP-NORMAL")
                )
            );
        final WorldPurchaseValidationServiceResponse validationResponse =
            worldPurchaseValidationService.validate(
                WorldPurchaseValidationServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId()
                )
            );
        final WorldForeclosureSignalProviderResponse positiveSignal =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    3,
                    true,
                    true
                )
            );
        final WorldForeclosureSignalProviderResponse negativeSignal =
            worldForeclosureSignalProviderService.getForeclosureSignal(
                WorldForeclosureSignalProviderRequest.of(
                    gameSession.getGameSessionId(),
                    2,
                    true,
                    true
                )
            );

        // then
        assertThat(reviewResponse.getContractResult()).isEqualTo(ContractResult.FAIL);
        assertThat(validationResponse.isPassed()).isFalse();
        assertThat(validationResponse.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED);
        assertThat(positiveSignal.isSignalGenerated()).isTrue();
        assertThat(negativeSignal.isSignalGenerated()).isFalse();
    }

    private PendingEventsProviderResponse.PendingEventItem findEventByTitle(
        final PendingEventsProviderResponse pendingEvents,
        final String title
    ) {
        return pendingEvents.getEvents().stream()
            .filter(event -> title.equals(event.getTitle()))
            .findFirst()
            .orElseThrow();
    }

    private PendingEventsProviderResponse.PendingEventChoiceItem findChoiceByCode(
        final PendingEventsProviderResponse.PendingEventItem event,
        final String choiceCode
    ) {
        return event.getChoices().stream()
            .filter(choice -> choiceCode.equals(choice.getChoiceCode()))
            .findFirst()
            .orElseThrow();
    }

    private GameSession createWorldSession(
        final Long userId,
        final int currentTurn,
        final CyclePhase cyclePhase,
        final Long targetPropertyId
    ) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cyclePhase
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        return gameSession;
    }

    private GameSession createSimpleSession(final Long userId, final Long targetPropertyId) {
        return GameSession.create(
            userId,
            1,
            "홍길동",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.MY_DATA
        );
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameCareer createGameCareer(final int gameSessionId, final int tenureTurns) {
        return GameCareer.builder()
            .gameId(gameSessionId)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(31_000_000)
            .tenureTurns(tenureTurns)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private RealEstateProperty createProperty(
        final String providerId,
        final String propertyName,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final List<ContractTrap> contractTraps
    ) {
        return RealEstateProperty.create(
            providerId,
            propertyName,
            "서울특별시 강남구 테헤란로 101",
            "11",
            "11680",
            Money.of(375_000_000L),
            latitude,
            longitude,
            HousingType.OWNED_APT,
            contractTraps
        );
    }

    private RealEstateDocument createDocument(
        final Long propertyId,
        final String imageUrl,
        final List<RealEstateDocumentChecklistItem> checklist
    ) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.CONTRACT,
            RealEstateRegistrySection.GAPGU,
            imageUrl,
            checklist,
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "기록사항 없음",
                        "2025년 1월 20일",
                        "없음",
                        "회귀 테스트용 문서",
                        Map.of()
                    )
                ),
                "해설",
                List.of("포인트"),
                "정답 해설",
                "오답 해설"
            )
        );
    }

    private ContractTrap createTrap(
        final String trapId,
        final String cash,
        final int stress
    ) {
        return ContractTrap.create(
            trapId,
            "TYPE",
            "CONTRACT",
            "설명",
            ContractTrapPenalty.create(cash, stress)
        );
    }
}
