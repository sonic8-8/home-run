package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.api.service.account.UserSsafyAccountSyncService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserMeService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;
    private final UserSsafyAccountSyncService userSsafyAccountSyncService;

    @Transactional
    public UserMeResponse getMe(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));

        final boolean isAssetLinked = user.hasSsafyLink()
                && userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent()
                && userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY).isPresent();
        if (isAssetLinked) {
            userSsafyAccountSyncService.syncLinkedAccounts(userId);
        }

        final UserFinancialSummary summary = resolveSummary(userId, isAssetLinked);

        return UserMeResponse.of(
                user.getId(),
                user.getEmail().getValue(),
                user.getName(),
                isAssetLinked,
                summary != null ? summary.getTotalAssetAmount() : null,
                summary != null ? summary.getNetAssetAmount() : null
        );
    }

    private UserFinancialSummary resolveSummary(final Long userId, final boolean isAssetLinked) {
        if (!isAssetLinked) {
            return null;
        }
        return userFinancialSummaryService.getSummary(userId);
    }
}
