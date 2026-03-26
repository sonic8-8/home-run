package io.ssafy.p.j14c103.homerun.api.controller.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class GameSessionApiRedTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        SecurityContextHolder.clearContext();
    }

    @DisplayName("세션 목록 조회는 빈 슬롯을 포함한 3개 저장 슬롯을 반환한다.")
    @Test
    void getSessions() throws Exception {
        // given
        final User user = saveUser("list-red@example.com");
        saveGameSession(user.getId(), 1, "윤서", CharacterType.FEMALE, JobType.STARTUP, 101L);
        saveGameSession(user.getId(), 3, "민지", CharacterType.FEMALE, JobType.MID_BIZ, 303L);
        saveGameSession(2L, 2, "타인세션", CharacterType.MALE, JobType.LARGE_BIZ, 202L);

        // when
        final String body = mockMvc.perform(get("/api/games/sessions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertThat(body).contains("\"slotNumber\":1");
        assertThat(body).contains("\"slotNumber\":2");
        assertThat(body).contains("\"slotNumber\":3");
        assertThat(body).doesNotContain("타인세션");
    }

    @DisplayName("세션 생성은 201을 반환하고 세션 메타데이터를 저장한다.")
    @Test
    void createSession() throws Exception {
        // given
        final User user = saveUser("create-red@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");

        // when
        mockMvc.perform(post("/api/games/sessions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(user.getId()))
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 1,
                      "characterType": "FEMALE",
                      "characterName": "승환",
                      "jobType": "SMALL_BIZ",
                      "regionCode": "11",
                      "districtCode": "11680",
                      "targetPropertyId": %d,
                      "useMyData": true
                    }
                    """.formatted(property.getPropertyId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201));

        // then
        assertThat(gameSessionRepository.count()).isEqualTo(1);
        final GameSession found = gameSessionRepository.findAll().get(0);
        assertThat(found.getUserId()).isEqualTo(user.getId());
        assertThat(found.getSlotNumber()).isEqualTo(1);
        assertThat(found.getCharacterName()).isEqualTo("승환");
        assertThat(found.getCharacterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(found.getJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(found.getRegionCode()).isEqualTo("11");
        assertThat(found.getDistrictCode()).isEqualTo("11680");
        assertThat(found.getTargetPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(found.getDataSourceType()).isEqualTo(DataSourceType.MY_DATA);
    }

    @DisplayName("이미 사용 중인 슬롯으로 세션 생성 시 409와 GAME_SLOT_CONFLICT를 반환한다.")
    @Test
    void createSessionWithOccupiedSlot() throws Exception {
        // given
        final User user = saveUser("conflict-red@example.com");
        saveGameSession(user.getId(), 1, "윤서", CharacterType.FEMALE, JobType.STARTUP, 101L);

        // when & then
        mockMvc.perform(post("/api/games/sessions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(user.getId()))
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 1,
                      "characterType": "MALE",
                      "characterName": "도윤",
                      "jobType": "LARGE_BIZ",
                      "regionCode": "11",
                      "districtCode": "11710",
                      "targetPropertyId": 202,
                      "useMyData": false
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SLOT_CONFLICT.getCode()));
    }

    @DisplayName("세션 상세 조회는 본인 세션 스냅샷을 반환한다.")
    @Test
    void getSessionDetail() throws Exception {
        // given
        final User user = saveUser("detail-red@example.com");
        final GameSession gameSession =
            saveGameSession(user.getId(), 2, "세준", CharacterType.MALE, JobType.SMALL_BIZ, 404L);

        // when
        final String body = mockMvc.perform(get("/api/games/sessions/{sessionId}", gameSession.getGameSessionId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertThat(body).contains("\"slotNumber\":2");
        assertThat(body).contains("세준");
    }

    @DisplayName("다른 사용자의 세션 상세 조회는 403과 GAME_SESSION_FORBIDDEN을 반환한다.")
    @Test
    void getOtherUsersSessionDetail() throws Exception {
        // given
        final User requester = saveUser("detail-other-red@example.com");
        final GameSession gameSession =
            saveGameSession(2L, 1, "타인세션", CharacterType.MALE, JobType.LARGE_BIZ, 202L);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}", gameSession.getGameSessionId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(requester.getId())))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()));
    }

    @DisplayName("세션 삭제는 본인 세션을 제거한다.")
    @Test
    void deleteSession() throws Exception {
        // given
        final User user = saveUser("delete-red@example.com");
        final GameSession gameSession =
            saveGameSession(user.getId(), 1, "윤서", CharacterType.FEMALE, JobType.STARTUP, 101L);

        // when
        mockMvc.perform(delete("/api/games/sessions/{sessionId}", gameSession.getGameSessionId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .with(currentUser(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200));

        // then
        assertThat(gameSessionRepository.findById(gameSession.getGameSessionId())).isEmpty();
    }

    private GameSession saveGameSession(
        final Long userId,
        final Integer slotNumber,
        final String characterName,
        final CharacterType characterType,
        final JobType jobType,
        final Long targetPropertyId
    ) {
        return gameSessionRepository.saveAndFlush(GameSession.create(
            userId,
            slotNumber,
            characterName,
            characterType,
            jobType,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        ));
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private RealEstateProperty saveTargetProperty(final String regionCode, final String districtCode) {
        housingRegionRepository.save(HousingRegion.create(regionCode, "서울특별시"));
        housingDistrictRepository.save(HousingDistrict.create(districtCode, regionCode, "강남구", "1168000000"));

        return realEstatePropertyRepository.saveAndFlush(RealEstateProperty.create(
            "red-provider-" + regionCode + districtCode,
            "테스트 매물",
            "서울특별시 강남구",
            regionCode,
            districtCode,
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.STUDIO,
            List.of()
        ));
    }

    private RequestPostProcessor currentUser(final Long userId) {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
            new AuthenticatedUser(userId, "user@example.com"),
            null,
            List.of()
        );

        return request -> {
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
