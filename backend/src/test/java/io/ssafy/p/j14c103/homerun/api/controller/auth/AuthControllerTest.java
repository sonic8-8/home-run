package io.ssafy.p.j14c103.homerun.api.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.auth.SignupService;
import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignupService signupService;

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
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @DisplayName("이미 가입된 이메일이면 409를 반환한다.")
    @Test
    void signupWithDuplicateEmail() throws Exception {
        // given
        given(signupService.signup(any(SignupServiceRequest.class)))
                .willThrow(HomerunException.from(ErrorCode.USER_EMAIL_DUPLICATE));

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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_EMAIL_DUPLICATE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_EMAIL_DUPLICATE.getMessage()));
    }

    @DisplayName("비밀번호 확인이 일치하지 않으면 400을 반환한다.")
    @Test
    void signupWithPasswordConfirmMismatch() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "홍길동",
                                  "email": "user@example.com",
                                  "password": "Password123!",
                                  "passwordConfirm": "Password1234!",
                                  "termsAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("약관 동의가 false이면 400을 반환한다.")
    @Test
    void signupWithoutTermsAgreement() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "홍길동",
                                  "email": "user@example.com",
                                  "password": "Password123!",
                                  "passwordConfirm": "Password123!",
                                  "termsAgreed": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
