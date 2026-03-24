package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.CreditScoreService;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(CreditScoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreditScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditScoreService creditScoreService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("신용 점수 조회는 ApiResponse로 감싼 신용 정보를 반환한다")
  @Test
  void getCreditScore() throws Exception {
    // given
    final CreditScoreResponse response = CreditScoreResponse.of(
            CreditScore.of(300, 250, 120, 90, 80),
            "A",
            12_345_678L,
            2_000_000L,
            10_345_678L
    );
    given(creditScoreService.getCreditScore(1L)).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/credit/score").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.score").value(840))
                .andExpect(jsonPath("$.data.grade").value(2))
                .andExpect(jsonPath("$.data.ratingName").value("A"))
                .andExpect(jsonPath("$.data.totalAsset").value(12345678))
                .andExpect(jsonPath("$.data.totalDebt").value(2000000))
                .andExpect(jsonPath("$.data.netAsset").value(10345678));
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
