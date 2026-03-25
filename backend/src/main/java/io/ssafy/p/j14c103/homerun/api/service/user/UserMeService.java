package io.ssafy.p.j14c103.homerun.api.service.user;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
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
    private final UserFinancialSummaryRepository userFinancialSummaryRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;

    public UserMeResponse getMe(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new HomerunException(ErrorCode.USER_NOT_FOUND));

        final boolean isAssetLinked = user.hasSsafyLink()
                && userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent()
                && userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY).isPresent();

        final Integer totalAssetAmount = resolveTotalAssetAmount(userId, isAssetLinked);

        return UserMeResponse.of(
                user.getId(),
                user.getEmail().getValue(),
                user.getName(),
                isAssetLinked,
                totalAssetAmount
        );
    }

    private Integer resolveTotalAssetAmount(final Long userId, final boolean isAssetLinked) {
        if (!isAssetLinked) {
            return null;
        }

        final UserFinancialSummary summary = userFinancialSummaryRepository.findById(userId)
                .orElseGet(() -> userFinancialSummaryService.getSummary(userId));
        return summary.getTotalAssetAmount();
    }
}
