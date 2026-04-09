package io.ssafy.p.j14c103.homerun.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.user.request.UserAssetLinkServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.client.kis.KisStockClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
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
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductSourceType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialTransactionType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialTransaction;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHolding;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDepositRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserHomeCreditScoreSnapshotRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

class UserAssetLinkServiceTest extends IntegrationTestSupport {

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
    private UserAssetProfileRepository userAssetProfileRepository;

    @Autowired
    private UserHomeCreditScoreSnapshotRepository userHomeCreditScoreSnapshotRepository;

    @Autowired
    private UserAssetDepositRepository userAssetDepositRepository;

    @Autowired
    private UserAssetLoanRepository userAssetLoanRepository;

    @Autowired
    private UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;

    @Autowired
    private UserAssetCardSpendRepository userAssetCardSpendRepository;

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
        userHomeCreditScoreSnapshotRepository.deleteAllInBatch();
        userAssetCardSpendRepository.deleteAllInBatch();
        userAssetOtherIncomeRepository.deleteAllInBatch();
        userAssetLoanRepository.deleteAllInBatch();
        userAssetDepositRepository.deleteAllInBatch();
        userAssetProfileRepository.deleteAllInBatch();
        financialProductTemplateRepository.deleteAllInBatch();
        stockMarketRepository.deleteAllInBatch();
        userAccountTransactionRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("첫 자산 연동에 성공하면 계좌와 온보딩 입력 기반 금융 데이터를 초기화한다.")
    @Test
    void linkAssets() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("userKey", "test-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("test-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0011111111111111"))
                .willReturn(Map.of("accountNo", "0012222222222222"));
        given(ssafyDemandDepositClient.depositAccount(eq("test-user-key"), eq("0011111111111111"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "59", "transactionDate", "20260326"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        final User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isMainAccountCreated()).isTrue();
        assertThat(response.isSeedmoneyAccountCreated()).isTrue();
        assertThat(response.isSummaryInitialized()).isTrue();
        assertThat(savedUser.getSsafyUserKey()).isEqualTo("test-user-key");
        assertThat(savedUser.getPaymentType()).isEqualTo("LIVING,TRANSPORT");

        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow();
        final UserAccount seedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY)
                .orElseThrow();
        assertThat(mainAccount.getBalanceSnapshot()).isEqualTo(request.getMainAccountBalanceAmount());
        assertThat(seedmoneyAccount.getBalanceSnapshot()).isZero();
        final ArgumentCaptor<Long> depositAmountCaptor = ArgumentCaptor.forClass(Long.class);
        then(ssafyDemandDepositClient).should()
                .depositAccount(eq("test-user-key"), eq("0011111111111111"), depositAmountCaptor.capture());
        assertThat(depositAmountCaptor.getValue()).isEqualTo((long) request.getMainAccountBalanceAmount());
        assertThat(userAccountTransactionRepository.findAll()).isEmpty();
        assertThat(userFinancialProductRepository.findByUserIdAndActiveYnTrue(user.getId()))
                .extracting(
                        product -> product.getProductType().name(),
                        UserFinancialProduct::getProductName,
                        UserFinancialProduct::getCurrentBalanceAmount,
                        product -> product.getSourceType().name()
                )
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("SAVING_DEPOSIT", "정기예금", 7_000_000L, "ASSET_LINK"),
                        org.assertj.core.groups.Tuple.tuple("SAVING_DEPOSIT", "청약저축", 1_500_000L, "ASSET_LINK"),
                        org.assertj.core.groups.Tuple.tuple("LOAN", "신용대출", 12_000_000L, "ASSET_LINK"),
                        org.assertj.core.groups.Tuple.tuple("LOAN", "학자금대출", 3_000_000L, "ASSET_LINK")
                );
        assertThat(ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(user.getId())).isEmpty();
        assertThat(userAssetDepositRepository.findAllByUserIdOrderByIdAsc(user.getId())).hasSize(2);
        assertThat(userAssetLoanRepository.findAllByUserIdOrderByIdAsc(user.getId())).hasSize(2);
        assertThat(userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(user.getId())).hasSize(2);
        assertThat(userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(user.getId())).hasSize(2);
        assertThat(userAssetProfileRepository.findById(user.getId())).isPresent()
                .get()
                .extracting(
                        profile -> profile.getMainAccountBalanceAmount(),
                        profile -> profile.getSalaryDayOfMonth(),
                        profile -> profile.getMonthlySalaryAmount(),
                        profile -> profile.getMonthlyFixedExpenseAmount(),
                        profile -> profile.getJobType()
                )
                .containsExactly(
                        request.getMainAccountBalanceAmount(),
                        request.getSalaryDayOfMonth(),
                        request.getMonthlySalaryAmount(),
                        request.getMonthlyFixedExpenseAmount(),
                        request.getJobType()
                );
        assertThat(userFinancialSummaryRepository.findById(user.getId())).isPresent()
                .get()
                .extracting(summary -> summary.getTotalAssetAmount(), summary -> summary.getTotalDebtAmount(), summary -> summary.getNetAssetAmount())
                .containsExactly(
                        11_500_000L,
                        15_000_000L,
                        -3_500_000L
                );
        assertThat(userHomeCreditScoreSnapshotRepository.findByUserIdAndScoreMonthStart(
                user.getId(),
                java.time.LocalDate.now().withDayOfMonth(1)
        )).isPresent();
    }

    @DisplayName("50억대 자산도 오버플로우 없이 저장하고 요약한다.")
    @Test
    void linkAssetsWithLargeAmounts() {
        // given
        final User user = saveUser("large-user@example.com");
        final UserAssetLinkServiceRequest request = largeAssetLinkRequest();
        given(ssafyMemberClient.createMember("large-user@example.com"))
                .willReturn(Map.of("userKey", "large-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("large-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0099999999999991"))
                .willReturn(Map.of("accountNo", "0099999999999992"));
        given(ssafyDemandDepositClient.depositAccount(eq("large-user-key"), eq("0099999999999991"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "60", "transactionDate", "20260328"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isSummaryInitialized()).isTrue();
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN))
                .isPresent()
                .get()
                .extracting(UserAccount::getBalanceSnapshot)
                .isEqualTo(5_000_000_000L);
        final ArgumentCaptor<Long> depositAmountCaptor = ArgumentCaptor.forClass(Long.class);
        then(ssafyDemandDepositClient).should()
                .depositAccount(eq("large-user-key"), eq("0099999999999991"), depositAmountCaptor.capture());
        assertThat(depositAmountCaptor.getValue()).isEqualTo(5_000_000_000L);
        assertThat(userAssetDepositRepository.findAllByUserIdOrderByIdAsc(user.getId()))
                .extracting(deposit -> deposit.getAmount())
                .containsExactly(3_500_000_000L, 2_800_000_000L);
        assertThat(userAssetLoanRepository.findAllByUserIdOrderByIdAsc(user.getId()))
                .extracting(loan -> loan.getAmount())
                .containsExactly(1_900_000_000L, 700_000_000L);
        assertThat(userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(user.getId()))
                .extracting(otherIncome -> otherIncome.getAmount())
                .containsExactly(150_000_000L, 70_000_000L);
        assertThat(userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(user.getId()))
                .extracting(cardSpend -> cardSpend.getAmount())
                .containsExactly(320_000_000L, 180_000_000L);
        assertThat(userAssetProfileRepository.findById(user.getId())).isPresent()
                .get()
                .extracting(
                        profile -> profile.getMainAccountBalanceAmount(),
                        profile -> profile.getMonthlySalaryAmount(),
                        profile -> profile.getMonthlyFixedExpenseAmount()
                )
                .containsExactly(5_000_000_000L, 3_200_000_000L, 1_100_000_000L);
        assertThat(userFinancialProductRepository.findByUserIdAndActiveYnTrue(user.getId()))
                .filteredOn(product -> product.getSourceType() == FinancialProductSourceType.ASSET_LINK)
                .extracting(
                        UserFinancialProduct::getProductName,
                        UserFinancialProduct::getProductType,
                        UserFinancialProduct::getCurrentBalanceAmount
                )
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("고액 예금", FinancialProductType.SAVING_DEPOSIT, 3_500_000_000L),
                        org.assertj.core.groups.Tuple.tuple("장기 적금", FinancialProductType.SAVING_DEPOSIT, 2_800_000_000L),
                        org.assertj.core.groups.Tuple.tuple("주택담보대출", FinancialProductType.LOAN, 1_900_000_000L),
                        org.assertj.core.groups.Tuple.tuple("전세대출", FinancialProductType.LOAN, 700_000_000L)
                );
        assertThat(userFinancialSummaryRepository.findById(user.getId())).isPresent()
                .get()
                .extracting(
                        summary -> summary.getTotalAssetAmount(),
                        summary -> summary.getTotalDebtAmount(),
                        summary -> summary.getNetAssetAmount(),
                        summary -> summary.getCashAssetAmount(),
                        summary -> summary.getSavingAssetAmount()
                )
                .containsExactly(
                        11_300_000_000L,
                        2_600_000_000L,
                        8_700_000_000L,
                        5_000_000_000L,
                        6_300_000_000L
                );
    }

    @DisplayName("SSAFY 회원 생성이 실패하면 회원 조회로 userKey를 확보한다.")
    @Test
    void linkAssetsWithMemberSearchFallback() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
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
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey()).isEqualTo("fallback-user-key");
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN)
                .orElseThrow();
        assertThat(mainAccount.getBalanceSnapshot()).isEqualTo(request.getMainAccountBalanceAmount());
    }

    @DisplayName("SSAFY 회원 생성 응답이 비어도 회원 조회로 userKey를 확보한다.")
    @Test
    void linkAssetsWithCreateMemberRuntimeExceptionFallback() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willThrow(new RuntimeException("SSAFY 회원 생성 응답이 없습니다."));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willReturn(Map.of("userKey", "fallback-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("fallback-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0015555555555555"))
                .willReturn(Map.of("accountNo", "0016666666666666"));
        given(ssafyDemandDepositClient.depositAccount(eq("fallback-user-key"), eq("0015555555555555"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "59", "transactionDate", "20260326"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey()).isEqualTo("fallback-user-key");
    }

    @DisplayName("SSAFY 회원 생성과 조회가 모두 네트워크 실패면 로컬 mock 연동으로 대체한다.")
    @Test
    void linkAssetsWithLocalMockFallbackWhenSsafyMemberApisFail() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willThrow(new RestClientException("create failed"));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willThrow(new RestClientException("search failed"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey())
                .isEqualTo("local-mock-user-" + user.getId());
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN))
                .isPresent()
                .get()
                .extracting(UserAccount::getAccountNumber, UserAccount::getBalanceSnapshot)
                .containsExactly("LOCAL-MAIN-" + user.getId(), request.getMainAccountBalanceAmount());
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY))
                .isPresent()
                .get()
                .extracting(UserAccount::getAccountNumber)
                .isEqualTo("LOCAL-SEEDMONEY-" + user.getId());
        verifyNoInteractions(ssafyDemandDepositClient);
    }

    @DisplayName("SSAFY 계좌 생성이 네트워크 실패면 로컬 mock 계좌로 대체한다.")
    @Test
    void linkAssetsWithLocalMockFallbackWhenAccountProvisionFails() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("userKey", "remote-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("remote-user-key", "test-account-type"))
                .willThrow(new RestClientException("account create failed"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey()).isEqualTo("remote-user-key");
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN))
                .isPresent()
                .get()
                .extracting(UserAccount::getAccountNumber)
                .isEqualTo("LOCAL-MAIN-" + user.getId());
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.SEEDMONEY))
                .isPresent()
                .get()
                .extracting(UserAccount::getAccountNumber)
                .isEqualTo("LOCAL-SEEDMONEY-" + user.getId());
    }

    @DisplayName("이미 연동된 사용자는 계좌를 중복 생성하지 않는다.")
    @Test
    void linkAssetsWithAlreadyLinkedUser() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        user.linkSsafy("existing-user-key", LocalDateTime.now());
        user.updatePaymentType("FUEL,MART");
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
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(response.isMainAccountCreated()).isFalse();
        assertThat(response.isSeedmoneyAccountCreated()).isFalse();
        assertThat(response.isSummaryInitialized()).isTrue();
        assertThat(userAccountRepository.findByUserId(user.getId())).hasSize(2);
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN))
                .isPresent()
                .get()
                .extracting(UserAccount::getBalanceSnapshot)
                .isEqualTo(request.getMainAccountBalanceAmount());
        assertThat(userRepository.findById(user.getId())).isPresent()
                .get()
                .extracting(User::getPaymentType)
                .isEqualTo("LIVING,TRANSPORT");
    }

    @DisplayName("재연동 시 자산 연동이 만든 금융상품만 교체하고 기존 시스템 상품은 유지한다.")
    @Test
    void linkAssetsReplacesOnlyAssetLinkManagedProducts() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
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

        final UserFinancialProduct systemSavingProduct = userFinancialProductRepository.saveAndFlush(UserFinancialProduct.create(
                user.getId(),
                FinancialProductType.SAVING_DEPOSIT,
                "KB국민은행",
                "기존 적금",
                4_000_000,
                LocalDateTime.now().minusMonths(6),
                FinancialProductSourceType.SYSTEM
        ));
        final UserFinancialProduct systemInvestmentProduct = userFinancialProductRepository.saveAndFlush(UserFinancialProduct.create(
                user.getId(),
                FinancialProductType.INVESTMENT,
                "한국투자증권",
                "기존 투자",
                1_200_000,
                LocalDateTime.now().minusMonths(4),
                FinancialProductSourceType.SYSTEM
        ));
        final UserFinancialProduct legacyAssetLinkProduct = userFinancialProductRepository.saveAndFlush(UserFinancialProduct.create(
                user.getId(),
                FinancialProductType.SAVING_DEPOSIT,
                "사용자 입력",
                "이전 예금",
                800_000,
                LocalDateTime.now().minusMonths(2)
        ));
        final UserFinancialProduct managedLoanProduct = userFinancialProductRepository.saveAndFlush(UserFinancialProduct.create(
                user.getId(),
                FinancialProductType.LOAN,
                "사용자 입력",
                "이전 대출",
                2_000_000,
                LocalDateTime.now().minusMonths(2),
                FinancialProductSourceType.ASSET_LINK
        ));
        userFinancialTransactionRepository.saveAndFlush(UserFinancialTransaction.create(
                user.getId(),
                systemInvestmentProduct.getId(),
                FinancialTransactionType.BUY,
                300_000,
                LocalDateTime.now().minusMonths(3)
        ));
        userFinancialTransactionRepository.saveAndFlush(UserFinancialTransaction.create(
                user.getId(),
                managedLoanProduct.getId(),
                FinancialTransactionType.REPAYMENT,
                50_000,
                LocalDateTime.now().minusMonths(1)
        ));
        userInvestmentHoldingRepository.saveAndFlush(UserInvestmentHolding.create(
                user.getId(),
                systemInvestmentProduct.getId(),
                "005930",
                3,
                80_000,
                90_000,
                LocalDateTime.now().minusDays(1)
        ));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userAccountRepository.findByUserIdAndAccountType(user.getId(), AccountType.MAIN))
                .isPresent()
                .get()
                .extracting(UserAccount::getBalanceSnapshot)
                .isEqualTo(request.getMainAccountBalanceAmount());
        final java.util.List<UserFinancialProduct> products = userFinancialProductRepository.findByUserIdAndActiveYnTrue(user.getId());
        assertThat(products).hasSize(6);
        assertThat(products)
                .filteredOn(product -> product.getProductName().equals("기존 적금"))
                .singleElement()
                .extracting(
                        UserFinancialProduct::getInstitutionName,
                        UserFinancialProduct::getProductType,
                        UserFinancialProduct::getCurrentBalanceAmount,
                        UserFinancialProduct::getSourceType
                )
                .containsExactly("KB국민은행", FinancialProductType.SAVING_DEPOSIT, 4_000_000L, FinancialProductSourceType.SYSTEM);
        assertThat(products)
                .filteredOn(product -> product.getProductName().equals("기존 투자"))
                .singleElement()
                .extracting(
                        UserFinancialProduct::getInstitutionName,
                        UserFinancialProduct::getProductType,
                        UserFinancialProduct::getSourceType
                )
                .containsExactly("한국투자증권", FinancialProductType.INVESTMENT, FinancialProductSourceType.SYSTEM);
        assertThat(products)
                .filteredOn(product -> product.getSourceType() == FinancialProductSourceType.ASSET_LINK)
                .extracting(
                        UserFinancialProduct::getProductName,
                        UserFinancialProduct::getProductType,
                        UserFinancialProduct::getCurrentBalanceAmount
                )
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("정기예금", FinancialProductType.SAVING_DEPOSIT, 7_000_000L),
                        org.assertj.core.groups.Tuple.tuple("청약저축", FinancialProductType.SAVING_DEPOSIT, 1_500_000L),
                        org.assertj.core.groups.Tuple.tuple("신용대출", FinancialProductType.LOAN, 12_000_000L),
                        org.assertj.core.groups.Tuple.tuple("학자금대출", FinancialProductType.LOAN, 3_000_000L)
                );
        assertThat(products)
                .extracting(UserFinancialProduct::getProductName)
                .doesNotContain("이전 예금", "이전 대출");
        assertThat(userFinancialTransactionRepository.findAll())
                .extracting(UserFinancialTransaction::getUserFinancialProductId)
                .contains(systemInvestmentProduct.getId())
                .doesNotContain(legacyAssetLinkProduct.getId(), managedLoanProduct.getId());
        assertThat(userInvestmentHoldingRepository.findByUserFinancialProductIdAndActiveYnTrue(systemInvestmentProduct.getId()))
                .hasSize(1);
        assertThat(userInvestmentHoldingRepository.findByUserFinancialProductIdAndActiveYnTrue(managedLoanProduct.getId()))
                .isEmpty();
    }

    @DisplayName("부분 연동 상태면 누락된 계좌만 복구한다.")
    @Test
    void linkAssetsWithPartialState() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
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
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

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
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        ReflectionTestUtils.setField(
                userAssetLinkService,
                "ssafyAccountProperties",
                SsafyAccountProperties.of("001", "한국은행", "")
        );

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId(), request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @DisplayName("SSAFY 회원 생성 응답에 userKey가 없어도 회원 조회로 복구한다.")
    @Test
    void linkAssetsWithMissingUserKey() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("status", "success"));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willReturn(Map.of("userKey", "fallback-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("fallback-user-key", "test-account-type"))
                .willReturn(Map.of("accountNo", "0017777777777777"))
                .willReturn(Map.of("accountNo", "0018888888888888"));
        given(ssafyDemandDepositClient.depositAccount(eq("fallback-user-key"), eq("0017777777777777"), anyLong()))
                .willReturn(Map.of("transactionUniqueNo", "59", "transactionDate", "20260326"));

        // when
        final UserAssetLinkResponse response = userAssetLinkService.linkAssets(user.getId(), request);

        // then
        assertThat(response.isAssetLinked()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getSsafyUserKey()).isEqualTo("fallback-user-key");
    }

    @DisplayName("SSAFY 회원 생성과 조회 모두 userKey를 주지 않으면 예외가 발생한다.")
    @Test
    void linkAssetsWithMissingUserKeyFromCreateAndSearch() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("status", "success"));
        given(ssafyMemberClient.searchMember("user@example.com"))
                .willReturn(Map.of("status", "success"));

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId(), request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    @DisplayName("SSAFY 계좌 생성 응답에 accountNo가 없으면 예외가 발생한다.")
    @Test
    void linkAssetsWithMissingAccountNumber() {
        // given
        final User user = saveUser("user@example.com");
        final UserAssetLinkServiceRequest request = assetLinkRequest();
        given(ssafyMemberClient.createMember("user@example.com"))
                .willReturn(Map.of("userKey", "test-user-key"));
        given(ssafyDemandDepositClient.createDemandDepositAccount("test-user-key", "test-account-type"))
                .willReturn(Map.of("status", "success"));

        // when & then
        assertThatThrownBy(() -> userAssetLinkService.linkAssets(user.getId(), request))
                .isInstanceOf(HomerunException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    private UserAssetLinkServiceRequest assetLinkRequest() {
        return UserAssetLinkServiceRequest.builder()
                .mainAccountBalanceAmount(3_000_000)
                .salaryDayOfMonth(25)
                .monthlySalaryAmount(4_200_000)
                .monthlyFixedExpenseAmount(1_800_000)
                .jobType(JobType.LARGE_BIZ)
                .depositItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("정기예금")
                                .amount(7_000_000)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("청약저축")
                                .amount(1_500_000)
                                .build()
                ))
                .loanItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("신용대출")
                                .amount(12_000_000)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("학자금대출")
                                .amount(3_000_000)
                                .build()
                ))
                .otherIncomeItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("부업")
                                .amount(300_000)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("임대수입")
                                .amount(200_000)
                                .build()
                ))
                .cardSpendItems(java.util.List.of(
                        UserAssetLinkServiceRequest.CardSpendItem.builder()
                                .category(io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory.LIVING)
                                .amount(250_000)
                                .build(),
                        UserAssetLinkServiceRequest.CardSpendItem.builder()
                                .category(io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory.TRANSPORT)
                                .amount(120_000)
                                .build()
                ))
                .paymentTypes(java.util.List.of("LIVING", "TRANSPORT"))
                .build();
    }

    private UserAssetLinkServiceRequest largeAssetLinkRequest() {
        return UserAssetLinkServiceRequest.builder()
                .mainAccountBalanceAmount(5_000_000_000L)
                .salaryDayOfMonth(25)
                .monthlySalaryAmount(3_200_000_000L)
                .monthlyFixedExpenseAmount(1_100_000_000L)
                .jobType(JobType.LARGE_BIZ)
                .depositItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("고액 예금")
                                .amount(3_500_000_000L)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("장기 적금")
                                .amount(2_800_000_000L)
                                .build()
                ))
                .loanItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("주택담보대출")
                                .amount(1_900_000_000L)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("전세대출")
                                .amount(700_000_000L)
                                .build()
                ))
                .otherIncomeItems(java.util.List.of(
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("임대수익")
                                .amount(150_000_000L)
                                .build(),
                        UserAssetLinkServiceRequest.NamedAmountItem.builder()
                                .name("배당수익")
                                .amount(70_000_000L)
                                .build()
                ))
                .cardSpendItems(java.util.List.of(
                        UserAssetLinkServiceRequest.CardSpendItem.builder()
                                .category(io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory.LIVING)
                                .amount(320_000_000L)
                                .build(),
                        UserAssetLinkServiceRequest.CardSpendItem.builder()
                                .category(io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory.TRANSPORT)
                                .amount(180_000_000L)
                                .build()
                ))
                .paymentTypes(java.util.List.of("LIVING", "TRANSPORT"))
                .build();
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
