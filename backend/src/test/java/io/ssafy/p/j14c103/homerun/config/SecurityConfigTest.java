package io.ssafy.p.j14c103.homerun.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.controller.auth.request.LoginRequest;
import io.ssafy.p.j14c103.homerun.api.controller.auth.request.SignupRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.LoginService;
import io.ssafy.p.j14c103.homerun.api.service.auth.RefreshAccessTokenService;
import io.ssafy.p.j14c103.homerun.api.service.auth.SignupService;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.LoginServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.RefreshAccessTokenServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.LoginResponse;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.RefreshAccessTokenResponse;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.CharacterQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterOptionsResponse;
import io.ssafy.p.j14c103.homerun.api.service.home.CreditScoreService;
import io.ssafy.p.j14c103.homerun.api.service.home.DashboardService;
import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.SpendingService;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassService;
import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private RefreshAccessTokenService refreshAccessTokenService;

    @MockitoBean
    private CharacterQueryService characterQueryService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private SpendingService spendingService;

    @MockitoBean
    private LoanRecommendationService loanRecommendationService;

    @MockitoBean
    private PassService passService;

    @MockitoBean
    private PassSavingService passSavingService;

    @MockitoBean
    private SeedmoneyService seedmoneyService;

    @MockitoBean
    private CreditScoreService creditScoreService;

    @DisplayName("회원가입 API는 인증 없이 접근할 수 있다.")
    @Test
    void signupEndpointIsPublic() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .passwordConfirm("Password123!")
                .termsAgreed(true)
                .build();
        SignupResponse response = SignupResponse.builder()
                .userId(1L)
                .email("user@example.com")
                .name("홍길동")
                .build();
        given(signupService.signup(any(SignupServiceRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @DisplayName("로그인 API는 인증 없이 접근할 수 있다.")
    @Test
    void loginEndpointIsPublic() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresIn(1800L)
                .build();
        given(loginService.login(any(LoginServiceRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @DisplayName("토큰 재발급 API는 인증 없이 접근할 수 있다.")
    @Test
    void refreshEndpointIsPublic() throws Exception {
        // given
        RefreshAccessTokenResponse response = RefreshAccessTokenResponse.builder()
                .accessToken("new-access-token")
                .accessTokenExpiresIn(1800L)
                .build();
        given(refreshAccessTokenService.refresh(any(RefreshAccessTokenServiceRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header(AUTHORIZATION, bearer("refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @DisplayName("비즈니스 API는 인증 없이 접근하면 401을 반환한다.")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("protectedPaths")
    void protectedApisRequireAuthentication(String path) throws Exception {
        // when & then
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));

        verifyNoInteractions(
                characterQueryService,
                dashboardService,
                spendingService,
                loanRecommendationService,
                passService,
                passSavingService,
                seedmoneyService,
                creditScoreService
        );
    }

    @DisplayName("유효한 Access Token이면 보호 API에 접근할 수 있다.")
    @Test
    void protectedApiAllowsValidAccessToken() throws Exception {
        // given
        CharacterOptionsResponse response = CharacterOptionsResponse.from(Stream.of(
                        CharacterOptionsResponse.CharacterOptionResponse.of(
                                CharacterType.FEMALE,
                                "/images/characters/female.png"
                        ),
                        CharacterOptionsResponse.CharacterOptionResponse.of(
                                CharacterType.MALE,
                                "/images/characters/male.png"
                        )
                ).toList());
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");
        given(characterQueryService.getCharacterOptions()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/characters")
                        .header(AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters[0].characterType").value("FEMALE"))
                .andExpect(jsonPath("$.characters[1].characterType").value("MALE"));
    }

    @DisplayName("유효한 Access Token이면 Principal 기반 보호 API에서 사용자 식별을 수행한다.")
    @Test
    void principalBasedProtectedApiResolvesAuthenticatedUser() throws Exception {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");
        SeedmoneyAccountResponse response = SeedmoneyAccountResponse.of("한국은행", "1234567890", 1000000);
        given(seedmoneyService.getAccount(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/seedmoney/account")
                        .header(AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.balance").value(1000000));

        verify(seedmoneyService).getAccount(1L);
    }

    @DisplayName("Refresh Token이면 보호 API 인증에 사용할 수 없다.")
    @Test
    void protectedApiRejectsRefreshToken() throws Exception {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(1L, "user@example.com");

        // when & then
        mockMvc.perform(get("/api/seedmoney/account")
                        .header(AUTHORIZATION, bearer(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_UNAUTHORIZED.getMessage()));

        verifyNoInteractions(seedmoneyService);
    }

    @DisplayName("비 API 경로는 인증이 있어도 접근할 수 없다.")
    @Test
    void nonApiPathIsDeniedEvenWithAuthentication() throws Exception {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");

        // when & then
        mockMvc.perform(get("/internal/health")
                        .header(AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden());
    }

    private static Stream<Arguments> protectedPaths() {
        return Stream.of(
                Arguments.of("/api/games/characters"),
                Arguments.of("/api/home/dashboard"),
                Arguments.of("/api/home/spending"),
                Arguments.of("/api/home/loan-recommendations"),
                Arguments.of("/api/pass/products"),
                Arguments.of("/api/pass/subscriptions"),
                Arguments.of("/api/pass/widget"),
                Arguments.of("/api/pass/history"),
                Arguments.of("/api/seedmoney/account"),
                Arguments.of("/api/credit/score"),
                Arguments.of("/api/future/protected-endpoint")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
