package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateDocumentService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateRegistryRandomService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.mockito.BDDMockito;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.sql.init.mode=never",
    "app.real-estate-import.enabled=false"
})
class RealEstateDocumentStartupCompatibilityTest {

    static final GenericContainer<?> POSTGRES_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("postgres:16-alpine")
    )
        .withEnv("POSTGRES_DB", "compat_test")
        .withEnv("POSTGRES_USER", "compat_user")
        .withEnv("POSTGRES_PASSWORD", "compat_password")
        .withExposedPorts(5432)
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("sql/legacy-real-estate-documents-null-property-id.sql"),
            "/docker-entrypoint-initdb.d/001-legacy-real-estate-documents.sql"
        )
        .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1))
        .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void overrideDatasourceProperties(final DynamicPropertyRegistry registry) {
        if (!POSTGRES_CONTAINER.isRunning()) {
            POSTGRES_CONTAINER.start();
        }

        registry.add(
            "spring.datasource.url",
            () -> "jdbc:postgresql://%s:%d/compat_test".formatted(
                POSTGRES_CONTAINER.getHost(),
                POSTGRES_CONTAINER.getMappedPort(5432)
            )
        );
        registry.add("spring.datasource.username", () -> "compat_user");
        registry.add("spring.datasource.password", () -> "compat_password");
    }

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @Autowired
    private RealEstateDocumentService realEstateDocumentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RealEstateRegistryRandomService realEstateRegistryRandomService;

    @DisplayName("legacy null property_id 샘플 row로 부팅한 뒤에도 등기부 조회 계약을 유지한다")
    @Test
    void getDocumentWithLegacyNullPropertyIdRowsAfterStartup() {
        // given
        final List<RealEstateDocument> gapguSamples = realEstateDocumentRepository
            .findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(RealEstateRegistrySection.GAPGU);
        final List<RealEstateDocument> eulguSamples = realEstateDocumentRepository
            .findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(RealEstateRegistrySection.EULGU);
        final User user = userRepository.save(User.register(
            Email.of("legacy-startup-user@example.com"),
            "tester",
            "hashed-password"
        ));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(
            RealEstateProperty.create(
                "LEGACY-SAMPLE-PROP",
                "레거시 샘플 매물",
                "서울특별시 강남구 테헤란로 1",
                "SEOUL",
                "GANGNAM",
                Money.of(850_000_000L),
                java.math.BigDecimal.valueOf(37.5010),
                java.math.BigDecimal.valueOf(127.0396),
                HousingType.OWNED_APT,
                List.of()
            )
        );
        final GameSession session = gameSessionRepository.saveAndFlush(createGameSession(user.getId(), property.getPropertyId()));

        realEstateDocumentRepository.saveAllAndFlush(List.of(
            RealEstateDocument.create(
                property.getPropertyId(),
                RealEstateDocumentType.REGISTRY,
                RealEstateRegistrySection.GAPGU,
                null,
                List.of(RealEstateDocumentChecklistItem.create("TRAP-LEGACY-001", "소유권 변동 확인", false)),
                null
            ),
            RealEstateDocument.create(
                property.getPropertyId(),
                RealEstateDocumentType.REGISTRY,
                RealEstateRegistrySection.EULGU,
                null,
                List.of(),
                null
            )
        ));
        BDDMockito.given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        BDDMockito.given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // then
        assertThat(gapguSamples).hasSize(1);
        assertThat(eulguSamples).hasSize(1);
        assertThat(gapguSamples.get(0).getPropertyId()).isNull();
        assertThat(eulguSamples.get(0).getPropertyId()).isNull();
        assertThat(readPropertyIdNullability()).isEqualTo("YES");

        // when
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            user.getId(),
            session.getGameSessionId(),
            property.getPropertyId()
        );

        // then
        assertThat(response.getGapguRows()).hasSize(1);
        assertThat(response.getGapguRows().get(0).getDetails()).contains("legacy gapgu sample");
        assertThat(response.getEulguRows()).hasSize(1);
        assertThat(response.getEulguRows().get(0).getDetails()).contains("legacy eulgu sample");
        assertThat(response.getChecklistItems())
            .extracting(RealEstateDocumentResponse.ChecklistItemResponse::getTrapId)
            .containsExactly("TRAP-LEGACY-001");
    }

    private String readPropertyIdNullability() {
        return jdbcTemplate.queryForObject(
            """
            select is_nullable
            from information_schema.columns
            where table_name = 'real_estate_documents'
              and column_name = 'property_id'
            """,
            String.class
        );
    }

    private GameSession createGameSession(final Long userId, final Long propertyId) {
        final GameSession session = GameSession.create(
            userId,
            1,
            "레거시",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            propertyId,
            DataSourceType.PROFILE
        );
        session.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 4, 1),
            CyclePhase.BOOM
        );
        return session;
    }
}
