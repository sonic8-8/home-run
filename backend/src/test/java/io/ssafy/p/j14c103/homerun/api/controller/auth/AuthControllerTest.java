package io.ssafy.p.j14c103.homerun.api.controller.auth;

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
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class AuthControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private RefreshAccessTokenService refreshAccessTokenService;

    @DisplayName("회원가입 요청이 성공하면 201과 사용자 응답을 반환한다.")
    @Test
    void signup() throws Exception {
        // given
        SignupResponse response = SignupResponse.builder()
                .userId(1L)
                .email("user@example.com")
                .name("홍길동")
                .build();
        given(signupService.signup(any(SignupServiceRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "홍길동",
                                  "email": "user@example.com",
                                  "password": "Password123!",
                                  "passwordConfirm": "Password123!",
                                  "termsAgreed": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andDo(document("auth/signup/success",
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인"),
                                fieldWithPath("termsAgreed").type(JsonFieldType.BOOLEAN).description("약관 동의 여부")
                        ),
                        apiResponseFields(
                                "회원가입 결과",
                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름")
                        )
                ));
    }

    @DisplayName("이미 가입된 이메일이면 409를 반환한다.")
    @Test
    void signupWithDuplicateEmail() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .passwordConfirm("Password123!")
                .termsAgreed(true)
                .build();
        given(signupService.signup(any(SignupServiceRequest.class)))
                .willThrow(new HomerunException(ErrorCode.USER_EMAIL_DUPLICATE));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_EMAIL_DUPLICATE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_EMAIL_DUPLICATE.getMessage()));
    }

    @DisplayName("비밀번호 확인이 일치하지 않으면 400을 반환한다.")
    @Test
    void signupWithPasswordConfirmMismatch() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .passwordConfirm("Password1234!")
                .termsAgreed(true)
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("$.errors[0].field").value("passwordConfirmed"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호 확인이 일치하지 않습니다."));
    }

    @DisplayName("약관 동의가 false이면 400을 반환한다.")
    @Test
    void signupWithoutTermsAgreement() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .passwordConfirm("Password123!")
                .termsAgreed(false)
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("$.errors[0].field").value("termsAgreed"))
                .andExpect(jsonPath("$.errors[0].message").value("약관 동의는 필수입니다."))
                .andDo(document("auth/signup/validation-error",
                        validationErrorResponseFields()
                ));
    }

    @DisplayName("로그인 요청이 성공하면 200과 토큰 응답을 반환한다.")
    @Test
    void login() throws Exception {
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(1800L))
                .andDo(document("auth/login/success",
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        apiResponseFields(
                                "로그인 결과",
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                fieldWithPath("accessTokenExpiresIn").type(JsonFieldType.NUMBER).description("액세스 토큰 만료까지 남은 초")
                        )
                ));
    }

    @DisplayName("이메일 또는 비밀번호가 올바르지 않으면 401을 반환한다.")
    @Test
    void loginWithInvalidCredentials() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();
        given(loginService.login(any(LoginServiceRequest.class)))
                .willThrow(new HomerunException(ErrorCode.AUTH_LOGIN_FAILED));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_LOGIN_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_LOGIN_FAILED.getMessage()));
    }

    @DisplayName("로그인 요청에서 비밀번호가 비어 있으면 400을 반환한다.")
    @Test
    void loginWithoutPassword() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("비밀번호는 필수입니다.")));
    }

    @DisplayName("유효한 Refresh Token이면 Access Token 재발급 응답을 반환한다.")
    @Test
    void refreshAccessToken() throws Exception {
        // given
        String authorizationHeader = "Bearer refresh-token";
        RefreshAccessTokenResponse response = RefreshAccessTokenResponse.builder()
                .accessToken("new-access-token")
                .accessTokenExpiresIn(1800L)
                .build();
        given(refreshAccessTokenService.refresh(any(RefreshAccessTokenServiceRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(1800L))
                .andDo(document("auth/refresh/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "토큰 재발급 결과",
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급된 액세스 토큰"),
                                fieldWithPath("accessTokenExpiresIn").type(JsonFieldType.NUMBER).description("액세스 토큰 만료까지 남은 초")
                        )
                ));
    }

    @DisplayName("유효하지 않은 Refresh Token이면 401을 반환한다.")
    @Test
    void refreshAccessTokenWithInvalidToken() throws Exception {
        // given
        String authorizationHeader = "Bearer invalid-refresh-token";
        given(refreshAccessTokenService.refresh(any(RefreshAccessTokenServiceRequest.class)))
                .willThrow(new HomerunException(ErrorCode.AUTH_REFRESH_INVALID));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_REFRESH_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_REFRESH_INVALID.getMessage()))
                .andDo(document("auth/refresh/unauthorized",
                        requestHeaders(authorizationHeader()),
                        basicErrorResponseFields()
                ));
    }

    @DisplayName("Refresh Token 헤더가 없으면 401을 반환한다.")
    @Test
    void refreshAccessTokenWithoutAuthorizationHeader() throws Exception {
        // given & when & then
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_REFRESH_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_REFRESH_INVALID.getMessage()));
    }
}
