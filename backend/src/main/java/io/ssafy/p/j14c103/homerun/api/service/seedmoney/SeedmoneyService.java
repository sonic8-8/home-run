package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.api.dto.seedmoney.SeedmoneyAccountResponse;
import io.ssafy.p.j14c103.homerun.api.dto.seedmoney.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.dto.seedmoney.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import io.ssafy.p.j14c103.homerun.infrastructure.ssafy.SsafyDemandDepositClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SeedmoneyService {

    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;

    public SeedmoneyService(
            final SeedmoneyAccountRepository seedmoneyAccountRepository,
            final SeedmoneyTransactionRepository seedmoneyTransactionRepository,
            final SsafyDemandDepositClient demandDepositClient) {
        this.seedmoneyAccountRepository = seedmoneyAccountRepository;
        this.seedmoneyTransactionRepository = seedmoneyTransactionRepository;
        this.demandDepositClient = demandDepositClient;
    }

    public SeedmoneyAccountResponse getAccount(final Long userId, final String userKey) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));

        final BigDecimal realTimeBalance = fetchRealTimeBalance(userKey, account.getAccountNumber());
        account.updateBalance(realTimeBalance);

        return SeedmoneyAccountResponse.of(
                account.getId(),
                account.getAccountNumber(),
                realTimeBalance,
                account.getUpdatedAt());
    }

    @Transactional
    public void transfer(final SeedmoneyTransferRequest request) {
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));

        demandDepositClient.transferAccount(
                request.getUserKey(),
                request.getToAccountNo(),
                account.getAccountNumber(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createTransfer(
                request.getUserId(),
                BigDecimal.valueOf(request.getAmount()),
                maskAccountNo(request.getToAccountNo()));

        seedmoneyTransactionRepository.save(transaction);
    }

    @Transactional
    public void deposit(final SeedmoneyDepositRequest request) {
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));

        demandDepositClient.transferAccount(
                request.getUserKey(),
                account.getAccountNumber(),
                request.getFromAccountNo(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createDeposit(
                request.getUserId(),
                BigDecimal.valueOf(request.getAmount()),
                maskAccountNo(request.getFromAccountNo()));

        seedmoneyTransactionRepository.save(transaction);
    }

    @SuppressWarnings("unchecked")
    private BigDecimal fetchRealTimeBalance(final String userKey, final String accountNo) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        return accounts.stream()
                .filter(account -> accountNo.equals(account.get("accountNo")))
                .findFirst()
                .map(account -> new BigDecimal(String.valueOf(account.get("accountBalance"))))
                .orElse(BigDecimal.ZERO);
    }

    private String maskAccountNo(final String accountNo) {
        if (accountNo == null || accountNo.length() < 4) {
            return accountNo;
        }
        return "****" + accountNo.substring(accountNo.length() - 4);
    }
}
