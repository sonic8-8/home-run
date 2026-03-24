package io.ssafy.p.j14c103.homerun.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardProduct;
import io.ssafy.p.j14c103.homerun.domain.card.CardProductRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductTemplate;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductTemplateRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.domain.user.AuthProvider;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SignupServiceTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountTransactionRepository userAccountTransactionRepository;

    @Autowired
    private FinancialProductTemplateRepository financialProductTemplateRepository;

    @Autowired
    private UserFinancialProductRepository userFinancialProductRepository;

    @Autowired
    private UserFinancialTransactionRepository userFinancialTransactionRepository;

    @Autowired
    private UserInvestmentHoldingRepository userInvestmentHoldingRepository;

    @Autowired
    private UserFinancialSummaryRepository userFinancialSummaryRepository;

    @Autowired
    private StockMarketRepository stockMarketRepository;

    @Autowired
    private CardProductRepository cardProductRepository;

    @Autowired
    private OwnedCardRepository ownedCardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SsafyMemberClient ssafyMemberClient;

    @MockitoBean
    private SsafyDemandDepositClient ssafyDemandDepositClient;

    @MockitoBean
    private KisStockClient kisStockClient;

    @AfterEach
    void tearDown() {
        cardTransactionRepository.deleteAllInBatch();
        ownedCardRepository.deleteAllInBatch();
        cardProductRepository.deleteAllInBatch();
        userInvestmentHoldingRepository.deleteAllInBatch();
        userFinancialTransactionRepository.deleteAllInBatch();
        userFinancialProductRepository.deleteAllInBatch();
        userFinancialSummaryRepository.deleteAllInBatch();
        financialProductTemplateRepository.deleteAllInBatch();
        stockMarketRepository.deleteAllInBatch();
        userAccountTransactionRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원가입에 성공하면 비밀번호를 해시해 사용자를 저장한다.")
    @Test
    void signup() {
        // given
        saveFinancialProductTemplates();
        saveStockMarkets();
        saveCardProducts();
        stubCurrentPrices();
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(java.util.Map.of("userKey", "test-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("test-user-key", "test-account-type"))
                .willReturn(java.util.Map.of("accountNo", "0011111111111111"))
                .willReturn(java.util.Map.of("accountNo", "0012222222222222"));
        given(ssafyDemandDepositClient.depositAccount("test-user-key", "0011111111111111", 10_000_000L))
                .willReturn(java.util.Map.of("status", "success"));

        // when
        SignupResponse response = signupService.signup(request);

        // then
        User savedUser = userRepository.findByEmail(Email.of("user@example.com"))
                .orElseThrow();
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getEmail()).isEqualTo(Email.of("user@example.com"));
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
        assertThat(savedUser.getSsafyUserKey()).isEqualTo("test-user-key");
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(savedUser.getId(), AccountType.MAIN)
                .orElseThrow();
        final UserAccount seedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(savedUser.getId(), AccountType.SEEDMONEY)
                .orElseThrow();
        assertThat(mainAccount.getBankCode()).isEqualTo("001");
        assertThat(mainAccount.getBankName()).isEqualTo("한국은행");
        assertThat(mainAccount.getBalanceSnapshot()).isEqualTo(10_000_000);
        assertThat(seedmoneyAccount.getBankCode()).isEqualTo("001");
        assertThat(seedmoneyAccount.getBankName()).isEqualTo("한국은행");
        assertThat(seedmoneyAccount.getBalanceSnapshot()).isZero();
        assertThat(userAccountTransactionRepository.findAll().size()).isGreaterThan(1);
        assertThat(userFinancialProductRepository.findByUserIdAndActiveYnTrue(savedUser.getId())).hasSize(3);
        final var investmentHoldings = userInvestmentHoldingRepository.findByUserIdAndActiveYnTrue(savedUser.getId());
        assertThat(investmentHoldings.size()).isBetween(3, 6);
        assertThat(userFinancialTransactionRepository.findAll()).isNotEmpty();
        final int investmentBalance = userFinancialProductRepository.findByUserIdAndActiveYnTrue(savedUser.getId()).stream()
                .filter(product -> product.getProductType() == FinancialProductType.INVESTMENT)
                .findFirst()
                .orElseThrow()
                .getCurrentBalanceAmount();
        final int holdingAmount = investmentHoldings.stream()
                .mapToInt(holding -> holding.getCurrentPriceAmount() * holding.getQuantity())
                .sum();
        assertThat(investmentBalance).isEqualTo(holdingAmount);
        assertThat(investmentHoldings)
                .allSatisfy(holding -> assertWithinYearRange(
                        holding.getStockCode(),
                        holding.getAveragePurchasePriceAmount()
                ));
        assertThat(userFinancialSummaryRepository.findById(savedUser.getId())).isPresent();
        assertThat(ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(savedUser.getId())).isNotEmpty();
        assertThat(cardTransactionRepository.findAll()).isNotEmpty();
    }

    @DisplayName("SSAFY 회원 생성이 실패하면 회원 조회로 userKey를 확보해 가입을 완료한다.")
    @Test
    void signupWithMemberSearchFallback() {
        // given
        saveFinancialProductTemplates();
        saveStockMarkets();
        saveCardProducts();
        stubCurrentPrices();
        final SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willThrow(new org.springframework.web.client.RestClientException("duplicate"));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willReturn(java.util.Map.of("userKey", "fallback-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("fallback-user-key", "test-account-type"))
                .willReturn(java.util.Map.of("accountNo", "0013333333333333"))
                .willReturn(java.util.Map.of("accountNo", "0014444444444444"));
        given(ssafyDemandDepositClient.depositAccount("fallback-user-key", "0013333333333333", 10_000_000L))
                .willReturn(java.util.Map.of("status", "success"));

        // when
        final SignupResponse response = signupService.signup(request);

        // then
        final User savedUser = userRepository.findByEmail(Email.of("user@example.com"))
                .orElseThrow();
        assertThat(response.getUserId()).isNotNull();
        assertThat(savedUser.getSsafyUserKey()).isEqualTo("fallback-user-key");
        assertThat(userAccountRepository.findByUserIdAndAccountType(savedUser.getId(), AccountType.MAIN)).isPresent();
        assertThat(userAccountRepository.findByUserIdAndAccountType(savedUser.getId(), AccountType.SEEDMONEY)).isPresent();
        assertThat(userFinancialSummaryRepository.findById(savedUser.getId())).isPresent();
    }

    @DisplayName("이미 가입된 이메일이면 예외가 발생한다.")
    @Test
    void signupWithDuplicateEmail() {
        // given
        userRepository.saveAndFlush(User.register(
                Email.of("user@example.com"),
                "기존 사용자",
                passwordEncoder.encode("Password123!")
        ));
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("홍길동")
                .email("user@example.com")
                .password("Password123!")
                .build();

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATE);
    }

    private void saveFinancialProductTemplates() {
        financialProductTemplateRepository.save(FinancialProductTemplate.create(
                FinancialProductType.SAVING_DEPOSIT,
                "KB국민은행",
                "KB Star 정기예금"
        ));
        financialProductTemplateRepository.save(FinancialProductTemplate.create(
                FinancialProductType.INVESTMENT,
                "한국투자증권",
                "뱅키스 주식거래계좌"
        ));
        financialProductTemplateRepository.save(FinancialProductTemplate.create(
                FinancialProductType.LOAN,
                "KB국민은행",
                "KB 직장인든든 신용대출"
        ));
    }

    private void saveStockMarkets() {
        stockMarketRepository.save(StockMarket.create(
                "005930",
                "삼성전자",
                "005930",
                "반도체",
                186_200,
                52_900,
                223_000,
                BigDecimal.valueOf(0.0215)
        ));
        stockMarketRepository.save(StockMarket.create(
                "000660",
                "SK하이닉스",
                "000660",
                "반도체",
                979_000,
                162_700,
                1_099_000,
                BigDecimal.valueOf(0.0280)
        ));
        stockMarketRepository.save(StockMarket.create(
                "005380",
                "현대차",
                "005380",
                "자동차",
                491_000,
                175_800,
                687_000,
                BigDecimal.valueOf(0.0220)
        ));
        stockMarketRepository.save(StockMarket.create(
                "035420",
                "NAVER",
                "035420",
                "플랫폼",
                212_500,
                176_200,
                295_000,
                BigDecimal.valueOf(0.0240)
        ));
        stockMarketRepository.save(StockMarket.create(
                "035720",
                "카카오",
                "035720",
                "플랫폼",
                47_700,
                36_300,
                71_600,
                BigDecimal.valueOf(0.0310)
        ));
        stockMarketRepository.save(StockMarket.create(
                "207940",
                "삼성바이오로직스",
                "207940",
                "바이오",
                1_629_000,
                1_501_000,
                1_987_000,
                BigDecimal.valueOf(0.0175)
        ));
    }

    private void assertWithinYearRange(final String stockCode, final Integer averagePurchasePriceAmount) {
        switch (stockCode) {
            case "005930" -> assertThat(averagePurchasePriceAmount).isBetween(52_900, 223_000);
            case "000660" -> assertThat(averagePurchasePriceAmount).isBetween(162_700, 1_099_000);
            case "005380" -> assertThat(averagePurchasePriceAmount).isBetween(175_800, 687_000);
            case "035420" -> assertThat(averagePurchasePriceAmount).isBetween(176_200, 295_000);
            case "035720" -> assertThat(averagePurchasePriceAmount).isBetween(36_300, 71_600);
            case "207940" -> assertThat(averagePurchasePriceAmount).isBetween(1_501_000, 1_987_000);
            default -> throw new IllegalArgumentException("예상하지 못한 종목코드입니다. stockCode=" + stockCode);
        }
    }

    private void stubCurrentPrices() {
        given(kisStockClient.getCurrentPrice("005930")).willReturn(186_200);
        given(kisStockClient.getCurrentPrice("000660")).willReturn(979_000);
        given(kisStockClient.getCurrentPrice("005380")).willReturn(491_000);
        given(kisStockClient.getCurrentPrice("035420")).willReturn(212_500);
        given(kisStockClient.getCurrentPrice("035720")).willReturn(47_700);
        given(kisStockClient.getCurrentPrice("207940")).willReturn(1_629_000);
    }

    private void saveCardProducts() {
        cardProductRepository.save(CardProduct.create(
                "KB국민 My WE:SH 카드",
                "KB국민카드",
                "생활비 중심 카드",
                300_000,
                30_000,
                """
                [
                  {
                    "categoryId": "CG-9ca85f66311a23d",
                    "categoryName": "생활",
                    "categoryDescription": "생활",
                    "discountRate": 10.0,
                    "exampleMerchants": ["스타벅스"]
                  },
                  {
                    "categoryId": "CG-4fa85f6455cad4a",
                    "categoryName": "교통",
                    "categoryDescription": "교통",
                    "discountRate": 5.0,
                    "exampleMerchants": ["택시"]
                  }
                ]
                """,
                "/images/cards/kb.png",
                true
        ));
        cardProductRepository.save(CardProduct.create(
                "신한 Mr.Life",
                "신한카드",
                "생활/통신 카드",
                300_000,
                30_000,
                """
                [
                  {
                    "categoryId": "CG-7fa85f6425bc311",
                    "categoryName": "통신",
                    "categoryDescription": "통신",
                    "discountRate": 10.0,
                    "exampleMerchants": ["SKT"]
                  }
                ]
                """,
                "/images/cards/shinhan.png",
                true
        ));
    }
}
