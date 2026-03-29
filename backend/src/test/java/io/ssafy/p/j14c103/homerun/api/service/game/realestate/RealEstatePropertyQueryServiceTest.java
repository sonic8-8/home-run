package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyListResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.TargetPropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldRealEstatePropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstatePropertyBoundsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyListProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertiesProviderResponse;
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
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RealEstatePropertyQueryServiceTest {

    @Autowired
    private RealEstatePropertyQueryService realEstatePropertyQueryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @MockitoBean
    private WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;

    @MockitoBean
    private TargetPropertyProviderService targetPropertyProviderService;

    @DisplayName("bounds가 있으면 world bounds provider 응답을 목록 응답으로 변환한다")
    @Test
    void getPropertiesWithBounds() {
        // given
        final User user = saveUser("real-estate-query-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        given(worldRealEstatePropertyProviderService.getPropertiesInBounds(org.mockito.ArgumentMatchers.any()))
            .willReturn(RealEstatePropertyListProviderResponse.from(
                java.util.List.of(
                    RealEstatePropertyListProviderResponse.PropertySummary.of(
                        7L,
                        "서초아트자이",
                        1_300_000_000L,
                        BigDecimal.valueOf(37.485551),
                        BigDecimal.valueOf(127.011500)
                    )
                )
            ));

        // when
        final RealEstatePropertyListResponse response = realEstatePropertyQueryService.getProperties(
            user.getId(),
            gameSession.getGameSessionId(),
            "126.9000,37.4000,127.1000,37.6000"
        );

        // then
        assertThat(response.getProperties()).hasSize(1);
        assertThat(response.getProperties().get(0).getPropertyId()).isEqualTo(7L);
        final ArgumentCaptor<RealEstatePropertyBoundsServiceRequest> requestCaptor =
            ArgumentCaptor.forClass(RealEstatePropertyBoundsServiceRequest.class);
        then(worldRealEstatePropertyProviderService).should().getPropertiesInBounds(requestCaptor.capture());
        assertThat(requestCaptor.getValue().minLatitude()).isEqualByComparingTo("37.4000");
        assertThat(requestCaptor.getValue().minLongitude()).isEqualByComparingTo("126.9000");
        assertThat(requestCaptor.getValue().maxLatitude()).isEqualByComparingTo("37.6000");
        assertThat(requestCaptor.getValue().maxLongitude()).isEqualByComparingTo("127.1000");
        then(targetPropertyProviderService).shouldHaveNoInteractions();
    }

    @DisplayName("bounds가 없으면 세션 지역 기준 target property provider를 호출한다")
    @Test
    void getPropertiesWithoutBounds() {
        // given
        final User user = saveUser("real-estate-query-no-bounds@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        given(targetPropertyProviderService.getTargetProperties("11", "11680"))
            .willReturn(TargetPropertiesProviderResponse.from(
                java.util.List.of(
                    TargetPropertiesProviderResponse.TargetPropertyItem.of(
                        11L,
                        "래미안 원베일리",
                        2_100_000_000L,
                        BigDecimal.valueOf(37.511100),
                        BigDecimal.valueOf(126.995200)
                    )
                )
            ));

        // when
        final RealEstatePropertyListResponse response = realEstatePropertyQueryService.getProperties(
            user.getId(),
            gameSession.getGameSessionId(),
            null
        );

        // then
        assertThat(response.getProperties()).hasSize(1);
        assertThat(response.getProperties().get(0).getName()).isEqualTo("래미안 원베일리");
        then(targetPropertyProviderService).should().getTargetProperties("11", "11680");
        then(worldRealEstatePropertyProviderService).shouldHaveNoInteractions();
    }

    @DisplayName("세션 매물 상세 조회는 world detail provider 응답을 반환한다")
    @Test
    void getPropertyDetail() {
        // given
        final User user = saveUser("real-estate-detail-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        given(worldRealEstatePropertyProviderService.getPropertyDetail(7L))
            .willReturn(RealEstatePropertyDetailProviderResponse.of(
                7L,
                "서초아트자이",
                1_300_000_000L,
                "서울특별시 서초구 반포대로 58",
                BigDecimal.valueOf(37.485551),
                BigDecimal.valueOf(127.011500),
                HousingType.OWNED_APT
            ));

        // when
        final RealEstatePropertyDetailResponse response = realEstatePropertyQueryService.getPropertyDetail(
            user.getId(),
            gameSession.getGameSessionId(),
            7L
        );

        // then
        assertThat(response.getPropertyId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("서초아트자이");
        assertThat(response.getHousingType()).isEqualTo(HousingType.OWNED_APT);
        then(worldRealEstatePropertyProviderService).should().getPropertyDetail(7L);
    }

    @DisplayName("다른 사용자의 세션 매물 조회는 GAME_SESSION_FORBIDDEN이 발생한다")
    @Test
    void getPropertiesForbidden() {
        // given
        final User owner = saveUser("real-estate-owner@example.com");
        final User requester = saveUser("real-estate-requester@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(owner.getId()));

        // when & then
        assertThatThrownBy(() -> realEstatePropertyQueryService.getProperties(
            requester.getId(),
            gameSession.getGameSessionId(),
            null
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(worldRealEstatePropertyProviderService).shouldHaveNoInteractions();
        then(targetPropertyProviderService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션의 매물 상세 조회는 GAME_SESSION_NOT_FOUND가 발생한다")
    @Test
    void getPropertyDetailWithUnknownSession() {
        // given
        final User user = saveUser("real-estate-detail-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> realEstatePropertyQueryService.getPropertyDetail(user.getId(), 9999L, 7L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(worldRealEstatePropertyProviderService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        return gameSession;
    }
}
