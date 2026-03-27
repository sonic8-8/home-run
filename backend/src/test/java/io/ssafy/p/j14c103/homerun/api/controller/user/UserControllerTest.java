package io.ssafy.p.j14c103.homerun.api.controller.user;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.user.request.UserAssetLinkServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAssetLinkService;
import io.ssafy.p.j14c103.homerun.api.service.user.UserMeService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserMeService userMeService;

    @MockitoBean
    private UserAssetLinkService userAssetLinkService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("내 정보 조회는 ApiResponse로 감싼 사용자 응답을 반환한다.")
    @Test
    void getMe() throws Exception {
        // given
        given(userMeService.getMe(1L)).willReturn(UserMeResponse.of(
                1L,
                "user@example.com",
                "홍길동",
                false,
                null,
                null
        ));

        // when & then
        mockMvc.perform(get("/api/users/me").with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.isAssetLinked").value(false))
                .andExpect(jsonPath("$.data.totalAssetAmount").isEmpty())
                .andExpect(jsonPath("$.data.netAssetAmount").isEmpty());
    }

    @DisplayName("자산 연동 실행은 ApiResponse로 감싼 연동 결과를 반환한다.")
    @Test
    void linkAssets() throws Exception {
        // given
        given(userAssetLinkService.linkAssets(eq(1L), any(UserAssetLinkServiceRequest.class))).willReturn(UserAssetLinkResponse.of(
                true,
                true,
                true,
                true
        ));

        // when & then
        mockMvc.perform(post("/api/users/me/asset-link")
                        .with(currentUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mainAccountBalanceAmount": 3000000,
                                  "salaryDayOfMonth": 25,
                                  "monthlySalaryAmount": 4200000,
                                  "monthlyFixedExpenseAmount": 1800000,
                                  "depositItems": [
                                    {"name": "정기예금", "amount": 7000000}
                                  ],
                                  "loanItems": [
                                    {"name": "신용대출", "amount": 12000000}
                                  ],
                                  "otherIncomeItems": [
                                    {"name": "부업", "amount": 300000}
                                  ],
                                  "cardSpendItems": [
                                    {"category": "LIVING", "amount": 200000},
                                    {"category": "TRANSPORT", "amount": 100000}
                                  ],
                                  "paymentTypes": ["LIVING", "TRANSPORT"],
                                  "jobType": "LARGE_BIZ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.isAssetLinked").value(true))
                .andExpect(jsonPath("$.data.mainAccountCreated").value(true))
                .andExpect(jsonPath("$.data.seedmoneyAccountCreated").value(true))
                .andExpect(jsonPath("$.data.summaryInitialized").value(true));
    }

    @DisplayName("자산 연동 요청에서 소비 선호 카테고리가 없으면 400을 반환한다.")
    @Test
    void linkAssetsWithoutPaymentTypes() throws Exception {
        mockMvc.perform(post("/api/users/me/asset-link")
                        .with(currentUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mainAccountBalanceAmount": 3000000,
                                  "salaryDayOfMonth": 25,
                                  "monthlySalaryAmount": 4200000,
                                  "monthlyFixedExpenseAmount": 1800000,
                                  "jobType": "LARGE_BIZ"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("paymentTypes")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("많이 쓰는 소비 분야는 최소 1개 이상 선택해야 합니다.")));
    }

    @DisplayName("자산 연동 요청에서 소비 선호 카테고리를 중복 선택하면 400을 반환한다.")
    @Test
    void linkAssetsWithDuplicatePaymentTypes() throws Exception {
        mockMvc.perform(post("/api/users/me/asset-link")
                        .with(currentUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mainAccountBalanceAmount": 3000000,
                                  "salaryDayOfMonth": 25,
                                  "monthlySalaryAmount": 4200000,
                                  "monthlyFixedExpenseAmount": 1800000,
                                  "paymentTypes": ["LIVING", "LIVING"],
                                  "jobType": "LARGE_BIZ"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].message", hasItem("많이 쓰는 소비 분야는 중복 선택할 수 없습니다.")));
    }

    private RequestPostProcessor currentUser() {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(1L, "user@example.com"),
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
