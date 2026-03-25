package io.ssafy.p.j14c103.homerun.api.controller.pass;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.pass.PassService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PassController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class PassControllerTest extends RestDocsTestSupport {

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
        mockMvc.perform(get("/api/pass/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.products[0].passId").value(1))
                .andExpect(jsonPath("$.data.products[0].name").value("커피 PASS"))
                .andDo(document("pass/products/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "PASS 상품 목록",
                                fieldWithPath("products").type(JsonFieldType.ARRAY).description("PASS 상품 목록"),
                                fieldWithPath("products[].passId").type(JsonFieldType.NUMBER).description("PASS 상품 ID"),
                                fieldWithPath("products[].name").type(JsonFieldType.STRING).description("PASS 상품명"),
                                fieldWithPath("products[].amountPerSave").type(JsonFieldType.NUMBER).description("1회 절약 금액"),
                                fieldWithPath("products[].description").type(JsonFieldType.STRING).description("PASS 상품 설명")
                        )
                ));
    }

    @DisplayName("PASS 구독 목록 조회는 Principal 기반으로 ApiResponse를 반환한다")
    @Test
    void getSubscriptions() throws Exception {
        // given
        final PassProduct coffeePass = createPassProduct(1L, "커피 PASS", 5000, "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, coffeePass, 5000, "0012345678");
        ReflectionTestUtils.setField(subscription, "id", 1L);
        given(passService.getSubscriptions(1L))
                .willReturn(List.of(PassSubscriptionResponse.from(
                        subscription,
                        65000,
                        List.of(true, true, true, false, false, false, false)
                )));

        // when & then
        mockMvc.perform(get("/api/pass/subscriptions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.subscriptions[0].subscriptionId").value(1))
                .andExpect(jsonPath("$.data.subscriptions[0].weeklyHistory[0]").value(true))
                .andDo(document("pass/subscriptions/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "PASS 구독 목록",
                                fieldWithPath("subscriptions").type(JsonFieldType.ARRAY).description("PASS 구독 목록"),
                                fieldWithPath("subscriptions[].subscriptionId").type(JsonFieldType.NUMBER).description("구독 ID"),
                                fieldWithPath("subscriptions[].passId").type(JsonFieldType.NUMBER).description("PASS 상품 ID"),
                                fieldWithPath("subscriptions[].name").type(JsonFieldType.STRING).description("PASS 상품명"),
                                fieldWithPath("subscriptions[].amountPerSave").type(JsonFieldType.NUMBER).description("1회 절약 금액"),
                                fieldWithPath("subscriptions[].totalSaved").type(JsonFieldType.NUMBER).description("누적 절약 금액"),
                                fieldWithPath("subscriptions[].weeklyHistory").type(JsonFieldType.ARRAY).description("주간 절약 이력"),
                                fieldWithPath("subscriptions[].subscribedAt").type(JsonFieldType.STRING).description("구독 시작 시각")
                        )
                ));
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
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
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
                .andExpect(jsonPath("$.data.passId").value(3))
                .andDo(document("pass/subscribe/success",
                        requestHeaders(authorizationHeader()),
                        requestFields(
                                fieldWithPath("passId").type(JsonFieldType.NUMBER).description("구독할 PASS 상품 ID"),
                                fieldWithPath("sourceAccountId").type(JsonFieldType.STRING).description("자동 이체 출금 계좌 ID")
                        ),
                        apiResponseFields(
                                "PASS 구독 정보",
                                fieldWithPath("subscriptionId").type(JsonFieldType.NUMBER).description("구독 ID"),
                                fieldWithPath("passId").type(JsonFieldType.NUMBER).description("PASS 상품 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("PASS 상품명"),
                                fieldWithPath("amountPerSave").type(JsonFieldType.NUMBER).description("1회 절약 금액"),
                                fieldWithPath("totalSaved").type(JsonFieldType.NUMBER).description("누적 절약 금액"),
                                fieldWithPath("weeklyHistory").type(JsonFieldType.ARRAY).description("주간 절약 이력"),
                                fieldWithPath("subscribedAt").type(JsonFieldType.STRING).description("구독 시작 시각")
                        )
                ));
    }

    @DisplayName("PASS 구독 신청에서 필수값이 없으면 400과 공통 에러 응답을 반환한다")
    @Test
    void subscribeWithoutPassId() throws Exception {
        // when & then
        mockMvc.perform(post("/api/pass/subscribe")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
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
                .andExpect(jsonPath("$.errors[*].message", hasItem("PASS 상품 ID는 필수입니다.")))
                .andDo(document("pass/subscribe/validation-error",
                        requestHeaders(authorizationHeader()),
                        validationErrorResponseFields()
                ));

        verifyNoInteractions(passService);
    }

    @DisplayName("PASS 구독 해지는 204 No Content를 반환한다")
    @Test
    void cancelSubscription() throws Exception {
        // given
        doNothing().when(passService).cancelSubscription(1L, 1L);

        // when & then
        mockMvc.perform(delete("/api/pass/subscriptions/{id}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isNoContent())
                .andDo(document("pass/subscriptions/cancel",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("id").description("PASS 구독 ID")
                        )
                ));
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
}
