package io.ssafy.p.j14c103.homerun.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.account.MainAccountInitialBalanceService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
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
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

@SpringBootTest
@ActiveProfiles("test")
class UserAssetLinkServiceTest {

    private static final int MIN_MAIN_INITIAL_BALANCE = 3_000_000;
    private static final int MAX_MAIN_INITIAL_BALANCE = 10_000_000;

    @Autowired
    private UserAssetLinkService userAssetLinkService;

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
    private SsafyAccountProperties ssafyAccountProperties;

    @Autowired
    private MainAccountInitialBalanceService mainAccountInitialBalanceService;

    @MockitoBean
    private SsafyMemberClient ssafyMemberClient;

    @MockitoBean
    private SsafyDemandDepositClient ssafyDemandDepositClient;

    @MockitoBean
    private KisStockClient kisStockClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userAssetLinkService, "ssafyAccountProperties", ssafyAccountProperties);
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.setField(userAssetLinkService, "ssafyAccountProperties", ssafyAccountProperties);
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

    @DisplayName("첫 자산 연동에 성공하면 계좌와 금융 mock 데이터를 초기화한다.")
    @Test
    void linkAssets() {
        // given
        saveFinancialProductTemplates();
        saveStockMarkets();
        saveCardProducts();
        stubCurrentPrices();
        final User user = saveUser("user@example.com");
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("userKey", "test-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("test-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0011111111111111"))
                .willReturn(Map.of("accountNo", "0012222222222222"));
        given(ssafyDemandDepositClient.depositAccount(eq("test-user-key"), eq("0011111111111111"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "59", "transactionDate", "20260326"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId());

        // then
        final User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isMainAccountCreated()).isTrue();
        assertThat(response.isSeedmoneyAccountCreated()).isTrue();
        assertThat(response.isSummaryInitialized()).isTrue();
        assertThat(savedUser.getSsafyUserKey()).isEqualTo("test-user-key");

        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow();
        final UserAccount seedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY)
                .orElseThrow();
        assertThat(mainAccount.getBalanceSnapshot()).isBetween(MIN_MAIN_INITIAL_BALANCE, MAX_MAIN_INITIAL_BALANCE);
        assertThat(mainAccount.getBalanceSnapshot() % 10_000).isZero();
        assertThat(mainAccount.getBalanceSnapshot())
                .isEqualTo(mainAccountInitialBalanceService.generateInitialBalance(user.getId()));
        assertThat(mainAccount.getMainInitialHistorySeeded()).isTrue();
        assertThat(seedmoneyAccount.getBalanceSnapshot()).isZero();
        final ArgumentCaptor<Long> depositAmountCaptor = ArgumentCaptor.forClass(Long.class);
        then(ssafyDemandDepositClient).should()
                .depositAccount(eq("test-user-key"), eq("0011111111111111"), depositAmountCaptor.capture());
        assertThat(depositAmountCaptor.getValue()).isEqualTo((long) mainAccount.getBalanceSnapshot());
        assertThat(userAccountTransactionRepository.findAll())
                .isNotEmpty();
        final int currentBalanceFromHistory = userAccountTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAccountType() == AccountType.MAIN)
                .mapToInt(transaction -> transaction.getTransactionType() == AccountTransactionType.WITHDRAW
                        ? -transaction.getAmount()
                        : transaction.getAmount())
                .sum();
        assertThat(currentBalanceFromHistory).isEqualTo(mainAccount.getBalanceSnapshot());
        assertThat(userAccountTransactionRepository.findAll())
                .anySatisfy(transaction -> assertThat(transaction.getTransactionSummary()).isEqualTo("초기 자산 설정"));
        assertThat(userFinancialProductRepository.findByUserIdAndActiveYnTrue(user.getId())).isNotEmpty();
        assertThat(ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(user.getId())).isNotEmpty();
        assertThat(userFinancialSummaryRepository.findById(user.getId())).isPresent();
    }

    @DisplayName("SSAFY 회원 생성이 실패하면 회원 조회로 userKey를 확보한다.")
    @Test
    void linkAssetsWithMemberSearchFallback() {
        // given
        saveFinancialProductTemplates();
        saveStockMarkets();
        saveCardProducts();
        stubCurrentPrices();
        final User user = saveUser("user@example.com");
        given(ssafyMemberClient.createMember("user@example.com"))
                .willThrow(new RestClientException("duplicate"));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willReturn(Map.of("userKey", "fallback-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("fallback-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0013333333333333"))
                .willReturn(Map.of("accountNo", "0014444444444444"));
        given(ssafyDemandDepositClient.depositAccount(eq("fallback-user-key"), eq("0013333333333333"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "59", "transactionDate", "20260326"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId());

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey()).isEqualTo("fallback-user-key");
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow();
        assertThat(mainAccount.getBalanceSnapshot()).isEqualTo(mainAccountInitialBalanceService.generateInitialBalance(user.getId()));
    }

    @DisplayName("이미 연동된 사용자는 계좌를 중복 생성하지 않는다.")
    @Test
    void linkAssetsWithAlreadyLinkedUser() {
        // given
        final User user = saveUser("user@example.com");
        user.linkSsafy("existing-user-key", LocalDateTime.now());
        userRepository.saveAndFlush(user);
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        ));
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.SEEDMONEY,
                "001",
                "한국은행",
                "0012222222222222",
                0
        ));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId());

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isMainAccountCreated()).isFalse();
        assertThat(response.isSeedmoneyAccountCreated()).isFalse();
        assertThat(response.isSummaryInitialized()).isFalse();
        assertThat(userAccountRepository.findByUserId(user.getId())).hasSize(2);
    }

    @DisplayName("부분 연동 상태면 누락된 계좌만 복구한다.")
    @Test
    void linkAssetsWithPartialState() {
        // given
        saveFinancialProductTemplates();
        saveStockMarkets();
        saveCardProducts();
        stubCurrentPrices();
        final User user = saveUser("user@example.com");
        user.linkSsafy("existing-user-key", LocalDateTime.now());
        userRepository.saveAndFlush(user);
        userAccountRepository.save(UserAccount.create(
                user.getId(),
                AccountType.MAIN,
                "001",
                "한국은행",
                "0011111111111111",
                10_000_000
        ));
        given(ssafyDemandDepositClient.createDemandDepositAccount("existing-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0012222222222222"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId());

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isMainAccountCreated()).isFalse();
        assertThat(response.isSeedmoneyAccountCreated()).isTrue();
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)).isPresent();
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY)).isPresent();
    }

    @DisplayName("계좌 상품 고유번호 설정이 비어 있으면 예외가 발생한다.")
    @Test
    void linkAssetsWithMissingAccountTypeUniqueNo() {
        // given
        final User user = saveUser("user@example.com");
        ReflectionTestUtils.setField(
                userAssetLinkService,
                "ssafyAccountProperties",
                SsafyAccountProperties.of("001", "한국은행", "")
        );

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId()))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @DisplayName("SSAFY 회원 응답에 userKey가 없으면 예외가 발생한다.")
    @Test
    void linkAssetsWithMissingUserKey() {
        // given
        final User user = saveUser("user@example.com");
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("status", "success"));

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId()))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    @DisplayName("SSAFY 계좌 생성 응답에 accountNo가 없으면 예외가 발생한다.")
    @Test
    void linkAssetsWithMissingAccountNumber() {
        // given
        final User user = saveUser("user@example.com");
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("userKey", "test-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("test-user-key", "test-account-type"))
                .willReturn(Map.of("status", "success"));

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId()))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    private User saveUser(final String email) {
        return userRepository.saveAndFlush(User.register(
                Email.of(email),
                "홍길동",
                "encoded-password"
        ));
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
