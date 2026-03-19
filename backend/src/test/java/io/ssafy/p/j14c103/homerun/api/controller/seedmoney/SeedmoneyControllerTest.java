package io.ssafy.p.j14c103.homerun.api.controller.seedmoney;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyTransactionResponse;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(SeedmoneyController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeedmoneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeedmoneyService seedmoneyService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("시드머니 계좌 생성은 201 ApiResponse를 반환한다")
  @Test
  void createAccount() throws Exception {
    // given
    given(seedmoneyService.createAccount(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
            .willReturn(SeedmoneyAccountResponse.of("한국은행", "110-123-000000", 10000000));

    // when & then
    mockMvc.perform(post("/api/seedmoney/create")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountTypeUniqueNo": "SEED-001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.accountNumber").value("110-123-000000"));
    }

  @DisplayName("시드머니 계좌 생성 요청에서 상품 고유번호가 비어 있으면 400과 공통 에러 응답을 반환한다")
  @Test
  void createAccountWithoutAccountTypeUniqueNo() throws Exception {
    // when & then
    mockMvc.perform(post("/api/seedmoney/create")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountTypeUniqueNo": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("accountTypeUniqueNo")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("상품 고유번호는 필수입니다.")));

        verifyNoInteractions(seedmoneyService);
    }

  @DisplayName("시드머니 계좌 조회는 ApiResponse를 반환한다")
  @Test
  void getAccount() throws Exception {
    // given
    given(seedmoneyService.getAccount(1L))
            .willReturn(SeedmoneyAccountResponse.of("한국은행", "110-123-000000", 300000));

    // when & then
    mockMvc.perform(get("/api/seedmoney/account").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bankName").value("한국은행"))
                .andExpect(jsonPath("$.data.balance").value(300000));
    }

  @DisplayName("시드머니 송금은 ApiResponse를 반환한다")
  @Test
  void transfer() throws Exception {
    // given
    given(seedmoneyService.transfer(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
            .willReturn(SeedmoneyTransactionResponse.of("TXN-001", 250000));

    // when & then
    mockMvc.perform(post("/api/seedmoney/transfer")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "toAccountNumber": "110-456-000000",
                                  "amount": 50000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.data.remainingBalance").value(250000));
    }

  @DisplayName("시드머니 송금 요청에서 금액이 없으면 400과 공통 에러 응답을 반환한다")
  @Test
  void transferWithoutAmount() throws Exception {
    // when & then
    mockMvc.perform(post("/api/seedmoney/transfer")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "toAccountNumber": "110-456-000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("amount")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("이체 금액은 필수입니다.")));

        verifyNoInteractions(seedmoneyService);
    }

  @DisplayName("시드머니 송금 요청에서 금액이 0이면 400과 공통 에러 응답을 반환한다")
  @Test
  void transferWithZeroAmount() throws Exception {
    // when & then
    mockMvc.perform(post("/api/seedmoney/transfer")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "toAccountNumber": "110-456-000000",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("amount")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("이체 금액은 0보다 커야 합니다.")));

        verifyNoInteractions(seedmoneyService);
    }

  @DisplayName("시드머니 입금은 ApiResponse를 반환한다")
  @Test
  void deposit() throws Exception {
    // given
    given(seedmoneyService.deposit(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
            .willReturn(SeedmoneyTransactionResponse.of("TXN-002", 400000));

    // when & then
    mockMvc.perform(post("/api/seedmoney/deposit")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountNumber": "110-789-000000",
                                  "amount": 100000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.transactionId").value("TXN-002"))
                .andExpect(jsonPath("$.data.remainingBalance").value(400000));
    }

  @DisplayName("시드머니 입금 요청에서 금액이 없으면 400과 공통 에러 응답을 반환한다")
  @Test
  void depositWithoutAmount() throws Exception {
    // when & then
    mockMvc.perform(post("/api/seedmoney/deposit")
                    .with(currentUser())
                    .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountNumber": "110-789-000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("amount")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("입금 금액은 필수입니다.")));

        verifyNoInteractions(seedmoneyService);
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
