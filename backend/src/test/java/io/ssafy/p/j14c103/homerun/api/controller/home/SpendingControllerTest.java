package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.SpendingService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
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

@WebMvcTest(SpendingController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpendingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpendingService spendingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("소비 분석 조회는 Principal 기반으로 ApiResponse를 반환한다")
  @Test
  void getSpending() throws Exception {
    // given
    final SpendingResponse response = SpendingResponse.of(
            "202603",
            Money.of(1_420_000L),
                List.of(SpendingCategoryDetail.of(
                        SpendingCategory.LIVING,
                        Money.of(450_000L),
                        Money.of(1_420_000L)
                ))
    );
    given(spendingService.getSpending(1L, "202603")).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/home/spending")
                    .param("month", "202603")
                    .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.month").value("202603"))
                .andExpect(jsonPath("$.data.totalExpense").value(1420000))
                .andExpect(jsonPath("$.data.categories[0].category").value("LIVING"))
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("생활"));
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
