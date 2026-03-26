package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.api.service.account.MainAccountInitialBalanceService;
import io.ssafy.p.j14c103.homerun.api.service.account.MainAccountInitialHistoryService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialMockDataService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.SeedmoneyAccountProjectionService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

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
    private final MainAccountInitialBalanceService mainAccountInitialBalanceService;
    private final MainAccountInitialHistoryService mainAccountInitialHistoryService;
    private final SeedmoneyAccountProjectionService seedmoneyAccountProjectionService;

    public UserAssetLinkResponse linkAssets(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));

        final boolean hasMainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent();
        final boolean hasSeedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .isPresent();

        if (user.hasSsafyLink() && hasMainAccount && hasSeedmoneyAccount) {
            return UserAssetLinkResponse.of(true, false, false, false);
        }

        validateAccountConfig();

        final String ssafyUserKey = resolveSsafyUserKey(user);
        boolean mainAccountCreated = false;
        boolean seedmoneyAccountCreated = false;

        if (!hasMainAccount) {
            createMainAccount(userId, ssafyUserKey);
            mainAccountCreated = true;
        }
        if (!hasSeedmoneyAccount) {
            createSeedmoneyAccount(userId, ssafyUserKey);
            seedmoneyAccountCreated = true;
        }

        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new IllegalStateException("주계좌 생성 후 조회에 실패했습니다."));
        mainAccountInitialHistoryService.seedInitialHistory(userId);
        userFinancialMockDataService.createInitialData(userId);
        userFinancialSummaryService.getSummary(userId);

        return UserAssetLinkResponse.of(true, mainAccountCreated, seedmoneyAccountCreated, true);
    }

    private void validateAccountConfig() {
        if (ssafyAccountProperties.getAccountTypeUniqueNo() == null
                || ssafyAccountProperties.getAccountTypeUniqueNo().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private String resolveSsafyUserKey(final User user) {
        if (user.hasSsafyLink()) {
            return user.getSsafyUserKey();
        }

        final String userKey = resolveSsafyUserKey(user.getEmail().getValue());
        user.linkSsafy(userKey, LocalDateTime.now());
        return userKey;
    }

    private String resolveSsafyUserKey(final String email) {
        try {
            return extractUserKey(ssafyMemberClient.createMember(email));
        } catch (final RestClientException exception) {
            return extractUserKey(ssafyMemberClient.searchMember(email));
        } catch (final HomerunException exception) {
            if (exception.getErrorCode() != ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID) {
                throw exception;
            }
            return extractUserKey(ssafyMemberClient.searchMember(email));
        } catch (final RuntimeException exception) {
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

    private void createMainAccount(final Long userId, final String ssafyUserKey) {
        final int initialBalance = mainAccountInitialBalanceService.generateInitialBalance(userId);
        final Map<String, Object> response = ssafyDemandDepositClient.createDemandDepositAccount(
                ssafyUserKey,
                ssafyAccountProperties.getAccountTypeUniqueNo()
        );
        final String accountNumber = extractAccountNumber(response);

        final Map<String, Object> depositResponse = ssafyDemandDepositClient.depositAccount(
                ssafyUserKey,
                accountNumber,
                initialBalance
        );
        final String bootstrapTransactionUniqueNo = extractTransactionUniqueNo(depositResponse);

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.MAIN,
                ssafyAccountProperties.getBankCode(),
                ssafyAccountProperties.getBankName(),
                accountNumber,
                initialBalance
        );
        account.initializeSsafySync(bootstrapTransactionUniqueNo);
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
