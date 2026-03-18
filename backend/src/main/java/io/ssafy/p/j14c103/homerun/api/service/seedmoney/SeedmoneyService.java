package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyTransactionResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeedmoneyService {

    private static final long INITIAL_SEEDMONEY = 10_000_000L; // 시드머니 초기 금액 1,000만원

    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;
    private final UserAuthContextService userAuthContextService;

    /**
     * SSAFY 가상 계좌 생성 + DB 저장 + 초기 시드머니 입금
     */
    @Transactional
    public SeedmoneyAccountResponse createAccount(
            final Long userId, final String accountTypeUniqueNo) {
        validateUserId(userId);
        if (seedmoneyAccountRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("이미 시드머니 계좌가 존재합니다.");
        }
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        // 1. SSAFY 수시입출금 계좌 생성
        final Map<String, Object> rec = demandDepositClient.createDemandDepositAccount(userKey, accountTypeUniqueNo);
        final String accountNo = (String) rec.get("accountNo");
        final String bankName = (String) rec.getOrDefault("bankName", "한국은행");

        // 2. DB에 계좌 저장
        final SeedmoneyAccount account = SeedmoneyAccount.create(userId, bankName, accountNo);
        seedmoneyAccountRepository.save(account);

        // 3. 초기 시드머니 입금
        demandDepositClient.depositAccount(userKey, accountNo, INITIAL_SEEDMONEY);

        final int balance = (int) INITIAL_SEEDMONEY;
        account.updateBalance(balance);

        return SeedmoneyAccountResponse.of(bankName, accountNo, balance);
    }

    public SeedmoneyAccountResponse getAccount(final Long userId) {
        validateUserId(userId);
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        final int realTimeBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(realTimeBalance);

        return SeedmoneyAccountResponse.of(
                account.getBankName(),
                account.getAccountNumber(),
                realTimeBalance);
    }

    @Transactional
    public SeedmoneyTransactionResponse transfer(final Long userId, final SeedmoneyTransferRequest request) {
        validateUserId(userId);
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        demandDepositClient.transferAccount(
                userKey,
                request.getToAccountNumber(),
                account.getAccountNumber(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createTransfer(
                userId,
                request.getAmount().intValue(),
                request.getToAccountNumber());

        seedmoneyTransactionRepository.save(transaction);

        final int remainingBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(remainingBalance);

        return SeedmoneyTransactionResponse.of(
                "TXN-" + transaction.getId(),
                remainingBalance);
    }

    @Transactional
    public SeedmoneyTransactionResponse deposit(final Long userId, final SeedmoneyDepositRequest request) {
        validateUserId(userId);
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        demandDepositClient.transferAccount(
                userKey,
                account.getAccountNumber(),
                request.getFromAccountNumber(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createDeposit(
                userId,
                request.getAmount().intValue(),
                request.getFromAccountNumber());

        seedmoneyTransactionRepository.save(transaction);

        final int remainingBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(remainingBalance);

        return SeedmoneyTransactionResponse.of(
                "TXN-" + transaction.getId(),
                remainingBalance);
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
}
