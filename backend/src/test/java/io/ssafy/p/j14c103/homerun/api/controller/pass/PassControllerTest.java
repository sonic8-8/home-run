package io.ssafy.p.j14c103.homerun.api.controller.pass;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.pass.PassService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PassController.class)
@AutoConfigureMockMvc(addFilters = false)
class PassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PassService passService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("PASS 상품 목록 조회는 ApiResponse로 감싼 목록을 반환한다")
  @Test
  void getProducts() throws Exception {
    // given
    final PassProduct coffeePass = createPassProduct(1L, "커피 PASS", 5000, "커피 한 잔 절약");
    given(passService.getProducts()).willReturn(List.of(PassProductResponse.from(coffeePass)));

    // when & then
    mockMvc.perform(get("/api/pass/products").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.products[0].passId").value(1))
                .andExpect(jsonPath("$.data.products[0].name").value("커피 PASS"));
    }

  @DisplayName("PASS 구독 목록 조회는 Principal 기반으로 ApiResponse를 반환한다")
  @Test
  void getSubscriptions() throws Exception {
    // given
    final PassProduct coffeePass = createPassProduct(1L, "커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, coffeePass, 5000, "0012345678");
    ReflectionTestUtils.setField(subscription, "id", 1L);
    given(passService.getSubscriptions(1L))
            .willReturn(List.of(PassSubscriptionResponse.from(subscription, 65000, List.of(true, true, true, false, false, false, false))));

    // when & then
    mockMvc.perform(get("/api/pass/subscriptions").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.subscriptions[0].subscriptionId").value(1))
                .andExpect(jsonPath("$.data.subscriptions[0].weeklyHistory[0]").value(true));
    }

  @DisplayName("PASS 구독 신청은 201 ApiResponse를 반환한다")
  @Test
  void subscribe() throws Exception {
    // given
    final PassProduct taxiPass = createPassProduct(3L, "택시 PASS", 10000, "택시 절약");
    final PassSubscription subscription = PassSubscription.create(1L, taxiPass, 10000, "110-123-000000");
    ReflectionTestUtils.setField(subscription, "id", 3L);
    given(passService.subscribe(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
            .willReturn(PassSubscriptionResponse.fromSubscribe(subscription));

    // when & then
    mockMvc.perform(post("/api/pass/subscribe")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "passId": 3,
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.subscriptionId").value(3))
                .andExpect(jsonPath("$.data.passId").value(3));
    }

  @DisplayName("PASS 구독 신청에서 필수값이 없으면 400과 공통 에러 응답을 반환한다")
  @Test
  void subscribeWithoutPassId() throws Exception {
    // when & then
    mockMvc.perform(post("/api/pass/subscribe")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("passId")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("PASS 상품 ID는 필수입니다.")));

        verifyNoInteractions(passService);
    }

  @DisplayName("PASS 구독 해지는 204 No Content를 반환한다")
  @Test
  void cancelSubscription() throws Exception {
    // given
    doNothing().when(passService).cancelSubscription(1L, 1L);

    // when & then
    mockMvc.perform(delete("/api/pass/subscriptions/1").with(currentUser()))
            .andExpect(status().isNoContent());
  }

    private PassProduct createPassProduct(
            final Long id,
            final String name,
            final int amountPerSave,
            final String description
    ) {
        final PassProduct product = PassProduct.create(name, amountPerSave, description);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
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
