package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyDepositRequest;
import io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request.SeedmoneyTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.seedmoney.response.SeedmoneyAccountResponse;
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

    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;

    public SeedmoneyAccountResponse getAccount(final Long userId, final String userKey) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));

        final int realTimeBalance = fetchRealTimeBalance(userKey, account.getMaskedAccountNo());
        account.updateBalance(realTimeBalance);

        return SeedmoneyAccountResponse.of(
                account.getId(),
                account.getBankName(),
                account.getMaskedAccountNo(),
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
                account.getMaskedAccountNo(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createTransfer(
                request.getUserId(),
                request.getAmount().intValue(),
                maskAccountNo(request.getToAccountNo()));

        seedmoneyTransactionRepository.save(transaction);
    }

    @Transactional
    public void deposit(final SeedmoneyDepositRequest request) {
        final SeedmoneyAccount account = seedmoneyAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다."));

        demandDepositClient.transferAccount(
                request.getUserKey(),
                account.getMaskedAccountNo(),
                request.getFromAccountNo(),
                request.getAmount());

        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createDeposit(
                request.getUserId(),
                request.getAmount().intValue(),
                maskAccountNo(request.getFromAccountNo()));

        seedmoneyTransactionRepository.save(transaction);
    }

    @SuppressWarnings("unchecked")
    private int fetchRealTimeBalance(final String userKey, final String accountNo) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);

        return accounts.stream()
                .filter(account -> accountNo.equals(account.get("accountNo")))
                .findFirst()
                .map(account -> Integer.parseInt(String.valueOf(account.get("accountBalance"))))
                .orElse(0);
    }

    private String maskAccountNo(final String accountNo) {
        if (accountNo == null || accountNo.length() < 4) {
            return accountNo;
        }
        return "****" + accountNo.substring(accountNo.length() - 4);
    }
}
