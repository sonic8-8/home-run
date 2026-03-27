package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.api.service.financial.request.UserFinancialInitializationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialMockDataService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.FicoCreditScoringService;
import io.ssafy.p.j14c103.homerun.api.service.user.request.UserAssetLinkServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyAccountProjectionService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpend;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDeposit;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDepositRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoan;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncome;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAssetLinkService {

    private final UserRepository userRepository;
    private final SsafyMemberClient ssafyMemberClient;
    private final SsafyDemandDepositClient ssafyDemandDepositClient;
    private final UserAccountRepository userAccountRepository;
    private final SsafyAccountProperties ssafyAccountProperties;
    private final UserFinancialMockDataService userFinancialMockDataService;
    private final UserFinancialSummaryService userFinancialSummaryService;
    private final FicoCreditScoringService ficoCreditScoringService;
    private final SeedmoneyAccountProjectionService seedmoneyAccountProjectionService;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetDepositRepository userAssetDepositRepository;
    private final UserAssetLoanRepository userAssetLoanRepository;
    private final UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;

    public UserAssetLinkResponse linkAssets(
            final Long userId,
            final UserAssetLinkServiceRequest request
    ) {
        log.info("사용자 자산 연동 시작. userId={}", userId);

        boolean hasSsafyLink = false;
        boolean hasMainAccount = false;
        boolean hasSeedmoneyAccount = false;
        boolean mainAccountCreated = false;
        boolean seedmoneyAccountCreated = false;

        try {
            final User user = userRepository.findById(userId)
                    .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));

            hasSsafyLink = user.hasSsafyLink();
            hasMainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent();
            hasSeedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                    .isPresent();
            final boolean hasAssetProfile = userAssetProfileRepository.findById(userId).isPresent();

            log.info(
                    "사용자 자산 연동 현재 상태. userId={}, hasSsafyLink={}, hasMainAccount={}, hasSeedmoneyAccount={}",
                    userId,
                    hasSsafyLink,
                    hasMainAccount,
                    hasSeedmoneyAccount
            );

            final boolean requiresAccountProvisioning = !hasSsafyLink || !hasMainAccount || !hasSeedmoneyAccount;
            final String ssafyUserKey;
            if (requiresAccountProvisioning) {
                validateAccountConfig();
                ssafyUserKey = resolveSsafyUserKey(user);
            } else {
                ssafyUserKey = user.getSsafyUserKey();
            }

            if (!hasMainAccount) {
                createMainAccount(userId, ssafyUserKey, request.getMainAccountBalanceAmount());
                mainAccountCreated = true;
                log.info("주계좌 생성 완료. userId={}", userId);
            }
            if (!hasSeedmoneyAccount) {
                createSeedmoneyAccount(userId, ssafyUserKey);
                seedmoneyAccountCreated = true;
                log.info("시드머니 계좌 생성 완료. userId={}", userId);
            }

            final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                    .orElseThrow(() -> new IllegalStateException("주계좌 생성 후 조회에 실패했습니다."));
            mainAccount.updateBalance(request.getMainAccountBalanceAmount());
            saveUserAssetProfile(userId, request);
            saveUserAssetItems(userId, request);
            userFinancialMockDataService.createInitialData(userId, toFinancialInitializationRequest(request));
            userFinancialSummaryService.getSummary(userId);
            ficoCreditScoringService.initializeOnboardingSnapshot(userId, !hasAssetProfile);

            final UserAssetLinkResponse response = UserAssetLinkResponse.of(
                    true,
                    mainAccountCreated,
                    seedmoneyAccountCreated,
                    true
            );
            log.info(
                    "사용자 자산 연동 완료. userId={}, mainAccountCreated={}, seedmoneyAccountCreated={}, summaryInitialized={}",
                    userId,
                    response.isMainAccountCreated(),
                    response.isSeedmoneyAccountCreated(),
                    response.isSummaryInitialized()
            );
            return response;
        } catch (final HomerunException exception) {
            log.error(
                    "사용자 자산 연동 실패. userId={}, errorCode={}, hasSsafyLink={}, hasMainAccount={}, hasSeedmoneyAccount={}, mainAccountCreated={}, seedmoneyAccountCreated={}",
                    userId,
                    exception.getErrorCode().getCode(),
                    hasSsafyLink,
                    hasMainAccount,
                    hasSeedmoneyAccount,
                    mainAccountCreated,
                    seedmoneyAccountCreated,
                    exception
            );
            throw exception;
        } catch (final RuntimeException exception) {
            log.error(
                    "사용자 자산 연동 실패. userId={}, hasSsafyLink={}, hasMainAccount={}, hasSeedmoneyAccount={}, mainAccountCreated={}, seedmoneyAccountCreated={}",
                    userId,
                    hasSsafyLink,
                    hasMainAccount,
                    hasSeedmoneyAccount,
                    mainAccountCreated,
                    seedmoneyAccountCreated,
                    exception
            );
            throw exception;
        }
    }

    private void validateAccountConfig() {
        if (ssafyAccountProperties.getAccountTypeUniqueNo() == null
                || ssafyAccountProperties.getAccountTypeUniqueNo().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private String resolveSsafyUserKey(final User user) {
        if (user.hasSsafyLink()) {
            log.info("기존 SSAFY 연동 정보 재사용. userId={}", user.getId());
            return user.getSsafyUserKey();
        }

        final String userKey = resolveSsafyUserKey(user.getId(), user.getEmail().getValue());
        user.linkSsafy(userKey, LocalDateTime.now());
        log.info("SSAFY 연동 정보 저장 완료. userId={}", user.getId());
        return userKey;
    }

    private String resolveSsafyUserKey(final Long userId, final String email) {
        try {
            return extractUserKey(ssafyMemberClient.createMember(email));
        } catch (final RestClientException exception) {
            log.warn("SSAFY 회원 생성 실패로 회원 조회를 시도합니다. userId={}", userId, exception);
            return extractUserKey(ssafyMemberClient.searchMember(email));
        } catch (final HomerunException exception) {
            if (exception.getErrorCode() != ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID) {
                throw exception;
            }
            log.warn("SSAFY 회원 생성 응답이 올바르지 않아 회원 조회를 시도합니다. userId={}", userId, exception);
            return extractUserKey(ssafyMemberClient.searchMember(email));
        } catch (final RuntimeException exception) {
            log.warn("SSAFY 회원 생성 중 예기치 않은 오류로 회원 조회를 시도합니다. userId={}", userId, exception);
            return extractUserKey(ssafyMemberClient.searchMember(email));
        }
    }

    private String extractUserKey(final Map<String, Object> response) {
        final Object value = response.get("userKey");
        if (value == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        final String userKey = String.valueOf(value);
        if (userKey.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return userKey;
    }

    private void saveUserAssetProfile(
            final Long userId,
            final UserAssetLinkServiceRequest request
    ) {
        final UserAssetProfile profile = userAssetProfileRepository.findById(userId)
                .orElseGet(() -> UserAssetProfile.create(
                        userId,
                        request.getMainAccountBalanceAmount(),
                        request.getSalaryDayOfMonth(),
                        request.getMonthlySalaryAmount(),
                        request.getMonthlyFixedExpenseAmount(),
                        request.getJobType()
                ));
        if (!profile.getUserId().equals(userId)) {
            throw new IllegalStateException("잘못된 자산 프로필 사용자 ID입니다.");
        }
        profile.update(
                request.getMainAccountBalanceAmount(),
                request.getSalaryDayOfMonth(),
                request.getMonthlySalaryAmount(),
                request.getMonthlyFixedExpenseAmount(),
                request.getJobType()
        );
        userAssetProfileRepository.save(profile);
    }

    private void saveUserAssetItems(
            final Long userId,
            final UserAssetLinkServiceRequest request
    ) {
        userAssetDepositRepository.deleteByUserId(userId);
        userAssetLoanRepository.deleteByUserId(userId);
        userAssetOtherIncomeRepository.deleteByUserId(userId);
        userAssetCardSpendRepository.deleteByUserId(userId);

        userAssetDepositRepository.saveAll(request.getDepositItems().stream()
                .map(item -> UserAssetDeposit.create(userId, item.getName(), item.getAmount()))
                .toList());
        userAssetLoanRepository.saveAll(request.getLoanItems().stream()
                .map(item -> UserAssetLoan.create(userId, item.getName(), item.getAmount()))
                .toList());
        userAssetOtherIncomeRepository.saveAll(request.getOtherIncomeItems().stream()
                .map(item -> UserAssetOtherIncome.create(userId, item.getName(), item.getAmount()))
                .toList());
        userAssetCardSpendRepository.saveAll(request.getCardSpendItems().stream()
                .map(item -> UserAssetCardSpend.create(userId, item.getCategory(), item.getAmount()))
                .toList());
    }

    private UserFinancialInitializationServiceRequest toFinancialInitializationRequest(
            final UserAssetLinkServiceRequest request
    ) {
        return UserFinancialInitializationServiceRequest.builder()
                .depositItems(request.getDepositItems().stream()
                        .map(item -> UserFinancialInitializationServiceRequest.NamedAmountItem.builder()
                                .name(item.getName())
                                .amount(item.getAmount())
                                .build())
                        .toList())
                .loanItems(request.getLoanItems().stream()
                        .map(item -> UserFinancialInitializationServiceRequest.NamedAmountItem.builder()
                                .name(item.getName())
                                .amount(item.getAmount())
                                .build())
                        .toList())
                .build();
    }

    private void createMainAccount(
            final Long userId,
            final String ssafyUserKey,
            final int initialBalance
    ) {
        final Map<String, Object> response = ssafyDemandDepositClient.createDemandDepositAccount(
                ssafyUserKey,
                ssafyAccountProperties.getAccountTypeUniqueNo()
        );
        final String accountNumber = extractAccountNumber(response);

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.MAIN,
                ssafyAccountProperties.getBankCode(),
                ssafyAccountProperties.getBankName(),
                accountNumber,
                initialBalance
        );
        if (initialBalance > 0) {
            final Map<String, Object> depositResponse = ssafyDemandDepositClient.depositAccount(
                    ssafyUserKey,
                    accountNumber,
                    initialBalance
            );
            account.initializeSsafySync(extractTransactionUniqueNo(depositResponse));
        } else {
            account.initializeSsafySync(null);
        }
        userAccountRepository.save(account);
    }

    private void createSeedmoneyAccount(final Long userId, final String ssafyUserKey) {
        final Map<String, Object> response = ssafyDemandDepositClient.createDemandDepositAccount(
                ssafyUserKey,
                ssafyAccountProperties.getAccountTypeUniqueNo()
        );
        final String accountNumber = extractAccountNumber(response);

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.SEEDMONEY,
                ssafyAccountProperties.getBankCode(),
                ssafyAccountProperties.getBankName(),
                accountNumber,
                0
        );
        account.initializeSsafySync(null);
        userAccountRepository.save(account);
        seedmoneyAccountProjectionService.syncFromUserAccount(account);
    }

    private String extractAccountNumber(final Map<String, Object> response) {
        final Object value = response.get("accountNo");
        if (value == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }

        final String accountNumber = String.valueOf(value);
        if (accountNumber.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return accountNumber;
    }

    private String extractTransactionUniqueNo(final Map<String, Object> response) {
        final Object value = response.get("transactionUniqueNo");
        if (value == null) {
            return null;
        }

        final String transactionUniqueNo = String.valueOf(value);
        if (transactionUniqueNo.isBlank()) {
            return null;
        }
        return transactionUniqueNo;
    }
}
