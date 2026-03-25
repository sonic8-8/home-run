package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

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
                0
        );
        userAccountRepository.save(account);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyAccountResponse.of(bankName, accountNo, 0);
    }

    public SeedmoneyAccountResponse getAccount(final Long userId) {
        validateUserId(userId);

        final UserAccount account = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);
        final int realTimeBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());

        account.updateBalance(realTimeBalance);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyAccountResponse.of(
                account.getBankName(),
                account.getAccountNumber(),
                realTimeBalance
        );
    }

    @Transactional
    public SeedmoneyTransactionResponse transfer(final Long userId, final SeedmoneyTransferServiceRequest request) {
        validateUserId(userId);

        final UserAccount account = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        demandDepositClient.transferAccount(
                userKey,
                request.getToAccountNumber(),
                account.getAccountNumber(),
                request.getAmount()
        );

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createTransfer(
                userId,
                request.getAmount().intValue(),
                request.getToAccountNumber()
        );
        seedmoneyTransactionRepository.save(transaction);
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.SEEDMONEY,
                null,
                AccountTransactionType.WITHDRAW,
                request.getAmount().intValue(),
                request.getToAccountNumber()
        ));

        final int remainingBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(remainingBalance);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyTransactionResponse.of("TXN-" + transaction.getId(), remainingBalance);
    }

    @Transactional
    public SeedmoneyTransactionResponse deposit(final Long userId, final SeedmoneyDepositServiceRequest request) {
        validateUserId(userId);

        final UserAccount account = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        demandDepositClient.transferAccount(
                userKey,
                account.getAccountNumber(),
                request.getFromAccountNumber(),
                request.getAmount()
        );

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createDeposit(
                userId,
                request.getAmount().intValue(),
                request.getFromAccountNumber()
        );
        seedmoneyTransactionRepository.save(transaction);
        userAccountTransactionRepository.save(UserAccountTransaction.create(
                userId,
                AccountType.SEEDMONEY,
                null,
                AccountTransactionType.DEPOSIT,
                request.getAmount().intValue(),
                request.getFromAccountNumber()
        ));

        final int remainingBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(remainingBalance);
        userFinancialSummaryService.getSummary(userId);

        return SeedmoneyTransactionResponse.of("TXN-" + transaction.getId(), remainingBalance);
    }

    private int fetchRealTimeBalance(final String userKey, final String accountNo) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        return accounts.stream()
                .filter(account -> accountNo.equals(account.get("accountNo")))
                .findFirst()
                .map(account -> Integer.parseInt(String.valueOf(account.get("accountBalance"))))
                .orElse(0);
    }

    private void validateUserId(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }

    private String resolveAccountTypeUniqueNo(final SeedmoneyCreateServiceRequest request) {
        if (request != null && request.getAccountTypeUniqueNo() != null && !request.getAccountTypeUniqueNo().isBlank()) {
            return request.getAccountTypeUniqueNo();
        }
        return ssafyAccountProperties.getAccountTypeUniqueNo();
    }
}
