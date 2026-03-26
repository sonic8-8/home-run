package io.ssafy.p.j14c103.homerun.api.service.seedmoney;

import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SeedmoneyAccountProjectionService {

    private final SeedmoneyAccountRepository seedmoneyAccountRepository;

    public void syncFromUserAccount(final UserAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("계좌 정보는 필수입니다.");
        }
        if (account.getAccountType() != AccountType.SEEDMONEY) {
            throw new IllegalArgumentException("시드머니 계좌만 동기화할 수 있습니다.");
        }

        final SeedmoneyAccount seedmoneyAccount = seedmoneyAccountRepository.findByUserId(account.getUserId())
                .orElseGet(() -> seedmoneyAccountRepository.save(SeedmoneyAccount.create(
                        account.getUserId(),
                        account.getBankName(),
                        account.getAccountNumber()
                )));
        seedmoneyAccount.syncSnapshot(
                account.getBankName(),
                account.getAccountNumber(),
                account.getBalanceSnapshot()
        );
    }
}
