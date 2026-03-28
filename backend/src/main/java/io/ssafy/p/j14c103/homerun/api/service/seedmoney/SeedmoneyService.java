package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.api.service.account.UserSsafyAccountSyncService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyCreateServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyDepositServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyTransactionResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.config.SsafyAccountProperties;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeedmoneyService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;
    private final UserAuthContextService userAuthContextService;
    private final UserFinancialSummaryService userFinancialSummaryService;
    private final SsafyAccountProperties ssafyAccountProperties;
    private final UserSsafyAccountSyncService userSsafyAccountSyncService;
    private final SeedmoneyAccountProjectionService seedmoneyAccountProjectionService;

    @Transactional
    public SeedmoneyAccountResponse createAccount(final Long userId, final SeedmoneyCreateServiceRequest request) {
        validateUserId(userId);
        if (userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY).isPresent()) {
            throw new IllegalStateException("이미 시드머니 계좌가 존재합니다.");
        }

        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);
        final Map<String, Object> rec = demandDepositClient.createDemandDepositAccount(
                userKey,
                resolveAccountTypeUniqueNo(request)
        );

        final String accountNo = (String) rec.get("accountNo");
        final String bankName = (String) rec.getOrDefault("bankName", ssafyAccountProperties.getBankName());

        final UserAccount account = UserAccount.create(
                userId,
                AccountType.SEEDMONEY,
                ssafyAccountProperties.getBankCode(),
                bankName,
                accountNo,
                0L
        );
        account.initializeSsafySync(null);
        userAccountRepository.save(account);
        seedmoneyAccountProjectionService.syncFromUserAccount(account);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyAccountResponse.of(bankName, accountNo, 0L);
    }

    @Transactional
    public SeedmoneyAccountResponse getAccount(final Long userId) {
        validateUserId(userId);
        userSsafyAccountSyncService.syncLinkedAccounts(userId);

        final UserAccount account = getRequiredSeedmoneyAccount(userId);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyAccountResponse.of(
                account.getBankName(),
                account.getAccountNumber(),
                account.getBalanceSnapshot()
        );
    }

    @Transactional
    public SeedmoneyTransactionResponse transfer(final Long userId, final SeedmoneyTransferServiceRequest request) {
        validateUserId(userId);

        final UserAccount account = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        final Map<String, Object> response = demandDepositClient.transferAccount(
                userKey,
                request.getToAccountNumber(),
                account.getAccountNumber(),
                request.getAmount()
        );
        final String seedmoneyTransactionUniqueNo = extractTransactionUniqueNo(response, account.getAccountNumber());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createTransfer(
                userId,
                request.getAmount(),
                request.getToAccountNumber()
        );
        seedmoneyTransactionRepository.save(transaction);
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.SEEDMONEY,
                null,
                AccountTransactionType.WITHDRAW,
                request.getAmount(),
                request.getToAccountNumber(),
                "시드머니 송금",
                seedmoneyTransactionUniqueNo,
                java.time.LocalDateTime.now()
        ));
        userSsafyAccountSyncService.advanceSyncBaseline(account, seedmoneyTransactionUniqueNo);
        userSsafyAccountSyncService.syncLinkedAccounts(userId);
        userFinancialSummaryService.getSummary(userId);
        final UserAccount refreshedAccount = getRequiredSeedmoneyAccount(userId);

        return SeedmoneyTransactionResponse.of("TXN-" + transaction.getId(), refreshedAccount.getBalanceSnapshot());
    }

    @Transactional
    public SeedmoneyTransactionResponse deposit(final Long userId, final SeedmoneyDepositServiceRequest request) {
        validateUserId(userId);

        final UserAccount account = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        final Map<String, Object> response = demandDepositClient.transferAccount(
                userKey,
                account.getAccountNumber(),
                request.getFromAccountNumber(),
                request.getAmount()
        );
        final String seedmoneyTransactionUniqueNo = extractTransactionUniqueNo(response, account.getAccountNumber());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createDeposit(
                userId,
                request.getAmount(),
                request.getFromAccountNumber()
        );
        seedmoneyTransactionRepository.save(transaction);
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.SEEDMONEY,
                null,
                AccountTransactionType.DEPOSIT,
                request.getAmount(),
                request.getFromAccountNumber(),
                "시드머니 입금",
                seedmoneyTransactionUniqueNo,
                java.time.LocalDateTime.now()
        ));
        final java.util.Optional<UserAccount> mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN);
        if (mainAccount.isPresent() && request.getFromAccountNumber().equals(mainAccount.get().getAccountNumber())) {
            final String mainTransactionUniqueNo = extractTransactionUniqueNo(response, mainAccount.get().getAccountNumber());
            userAccountTransactionRepository.save(UserAccountTransaction.create(
                    userId,
                    AccountType.MAIN,
                    null,
                    AccountTransactionType.INTERNAL_TRANSFER,
                    request.getAmount(),
                    account.getAccountNumber(),
                    "시드머니 입금",
                    mainTransactionUniqueNo,
                    java.time.LocalDateTime.now()
            ));
            userSsafyAccountSyncService.advanceSyncBaseline(mainAccount.get(), mainTransactionUniqueNo);
        }

        userSsafyAccountSyncService.advanceSyncBaseline(account, seedmoneyTransactionUniqueNo);
        userSsafyAccountSyncService.syncLinkedAccounts(userId);
        userFinancialSummaryService.getSummary(userId);
        final UserAccount refreshedAccount = getRequiredSeedmoneyAccount(userId);

        return SeedmoneyTransactionResponse.of("TXN-" + transaction.getId(), refreshedAccount.getBalanceSnapshot());
    }

    private void validateUserId(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }

    private UserAccount getRequiredSeedmoneyAccount(final Long userId) {
        return userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
    }

    private String resolveAccountTypeUniqueNo(final SeedmoneyCreateServiceRequest request) {
        if (request != null && request.getAccountTypeUniqueNo() != null && !request.getAccountTypeUniqueNo().isBlank()) {
            return request.getAccountTypeUniqueNo();
        }
        return ssafyAccountProperties.getAccountTypeUniqueNo();
    }

    private String extractTransactionUniqueNo(
            final Map<String, Object> response,
            final String accountNo
    ) {
        final Object rec = response.get("REC");
        if (!(rec instanceof List<?> records)) {
            return null;
        }

        return records.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .filter(record -> accountNo.equals(String.valueOf(record.get("accountNo"))))
                .map(record -> record.get("transactionUniqueNo"))
                .filter(java.util.Objects::nonNull)
                .map(String::valueOf)
                .findFirst()
                .orElse(null);
    }
}
