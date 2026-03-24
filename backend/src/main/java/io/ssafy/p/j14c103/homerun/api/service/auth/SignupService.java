package io.ssafy.p.j14c103.homerun.api.service.auth;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialMockDataService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyMemberClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SsafyMemberClient ssafyMemberClient;
    private final SsafyDemandDepositClient ssafyDemandDepositClient;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final SsafyAccountProperties ssafyAccountProperties;
    private final UserFinancialMockDataService userFinancialMockDataService;
    private final UserFinancialSummaryService userFinancialSummaryService;

    public SignupResponse signup(final SignupServiceRequest request) {
        final Email email = Email.of(request.getEmail());

        validateDuplicateEmail(email);
        validateAccountConfig();

        final String ssafyUserKey = resolveSsafyUserKey(email.getValue());

        final User user = request.toEntity(
                email,
                passwordEncoder.encode(request.getPassword())
        );
        user.linkSsafy(ssafyUserKey, LocalDateTime.now());

        final User savedUser = userRepository.saveAndFlush(user);

        createMainAccount(savedUser.getId(), ssafyUserKey);
        createSeedmoneyAccount(savedUser.getId(), ssafyUserKey);
        userFinancialMockDataService.createInitialData(savedUser.getId());
        userFinancialSummaryService.getSummary(savedUser.getId());

        return SignupResponse.of(savedUser);
    }

    private void validateDuplicateEmail(final Email email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            return;
        }

        throw new HomerunException(ErrorCode.USER_EMAIL_DUPLICATE);
    }

    private void validateAccountConfig() {
        if (ssafyAccountProperties.getAccountTypeUniqueNo() == null
                || ssafyAccountProperties.getAccountTypeUniqueNo().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private String resolveSsafyUserKey(final String email) {
        try {
            return extractUserKey(ssafyMemberClient.createMember(email));
        } catch (RestClientException exception) {
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
        final Map<String, Object> response = ssafyDemandDepositClient.createDemandDepositAccount(
                ssafyUserKey,
                ssafyAccountProperties.getAccountTypeUniqueNo());
        final String accountNumber = extractAccountNumber(response);

        ssafyDemandDepositClient.depositAccount(
                ssafyUserKey,
                accountNumber,
                ssafyAccountProperties.getMainInitialBalance());

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.MAIN,
                ssafyAccountProperties.getBankCode(),
                ssafyAccountProperties.getBankName(),
                accountNumber,
                (int) ssafyAccountProperties.getMainInitialBalance());
        userAccountRepository.save(account);

        final UserAccountTransaction transaction = UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                null,
                AccountTransactionType.DEPOSIT,
                (int) ssafyAccountProperties.getMainInitialBalance(),
                null);
        userAccountTransactionRepository.save(transaction);
    }

    private void createSeedmoneyAccount(final Long userId, final String ssafyUserKey) {
        final Map<String, Object> response = ssafyDemandDepositClient.createDemandDepositAccount(
                ssafyUserKey,
                ssafyAccountProperties.getAccountTypeUniqueNo());
        final String accountNumber = extractAccountNumber(response);

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.SEEDMONEY,
                ssafyAccountProperties.getBankCode(),
                ssafyAccountProperties.getBankName(),
                accountNumber,
                0);
        userAccountRepository.save(account);
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
}
