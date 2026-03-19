package io.ssafy.p.j14c103.homerun.api.controller.pass;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PassSavingController.class)
@AutoConfigureMockMvc(addFilters = false)
class PassSavingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PassSavingService passSavingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("즉시 저축은 ApiResponse로 감싼 결과를 반환한다")
  @Test
  void save() throws Exception {
    // given
    given(passSavingService.save(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
            .willReturn(PassSaveResponse.of(5000, 70000, 295000));

    // when & then
    mockMvc.perform(post("/api/pass/save")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "subscriptionId": 1,
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.savedAmount").value(5000))
                .andExpect(jsonPath("$.data.totalSaved").value(70000));
    }

  @DisplayName("즉시 저축 요청에서 필수값이 없으면 400과 공통 에러 응답을 반환한다")
  @Test
  void saveWithoutSubscriptionId() throws Exception {
    // when & then
    mockMvc.perform(post("/api/pass/save")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("subscriptionId")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("구독 ID는 필수입니다.")));

        verifyNoInteractions(passSavingService);
    }

  @DisplayName("PASS 위젯 조회는 ApiResponse를 반환한다")
  @Test
  void getWidget() throws Exception {
    // given
    given(passSavingService.getWidget(1L)).willReturn(PassWidgetResponse.of(45000, 182000, 500000));

    // when & then
    mockMvc.perform(get("/api/pass/widget").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.todaySaved").value(45000))
                .andExpect(jsonPath("$.data.remaining").value(318000));
    }

  @DisplayName("PASS 이력 조회는 ApiResponse로 감싼 페이지 결과를 반환한다")
  @Test
  void getHistory() throws Exception {
    // given
    final SeedmoneyTransaction transaction = SeedmoneyTransaction.createSave(1L, 1L, 5000);
    ReflectionTestUtils.setField(transaction, "id", 1L);
    final Page<PassHistoryResponse> page = new PageImpl<>(
                List.of(PassHistoryResponse.from(transaction)),
                PageRequest.of(0, 20),
                1
    );
    given(passSavingService.getHistory(1L, 0, 20)).willReturn(page);

    // when & then
    mockMvc.perform(get("/api/pass/history")
                    .param("page", "0")
                    .param("size", "20")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("SAVE"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
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
