package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSubscribeServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassService {

    private final PassProductRepository passProductRepository;
    private final PassSubscriptionRepository passSubscriptionRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final UserAccountRepository userAccountRepository;
    private final PassProductSeedService passProductSeedService;

    public List<PassProductResponse> getProducts() {
        passProductSeedService.ensureDefaultProducts();

        return passProductRepository.findAllByOrderByIdAsc().stream()
                .map(PassProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PassSubscriptionResponse> getSubscriptions(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final List<PassSubscription> subscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);

        return subscriptions.stream()
                .map(sub -> {
                    final int totalSaved = calculateTotalSaved(userId, sub);
                    final List<Boolean> weeklyHistory = calculateWeeklyHistory(userId, sub);
                    return PassSubscriptionResponse.from(sub, totalSaved, weeklyHistory);
                })
                .toList();
    }

    @Transactional
    public PassSubscriptionResponse subscribe(final Long userId, final PassSubscribeServiceRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        passProductSeedService.ensureDefaultProducts();

        final PassProduct product = passProductRepository.findById(request.getPassId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 PASS 상품입니다."));
        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new IllegalArgumentException("주계좌가 없습니다."));
        final String sourceAccountNo = resolveSourceAccountNo(mainAccount, request.getSourceAccountId());

        final PassSubscription subscription = PassSubscription.create(
                userId, product, product.getAmountPerSave(), sourceAccountNo);

        return PassSubscriptionResponse.fromSubscribe(passSubscriptionRepository.save(subscription));
    }

    @Transactional
    public void cancelSubscription(final Long userId, final Long subscriptionId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final PassSubscription subscription = passSubscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다."));

        if (!subscription.getIsActive()) {
            throw new IllegalStateException("이미 해지된 구독입니다.");
        }

        subscription.cancel();
    }

    private int calculateTotalSaved(final Long userId, final PassSubscription subscription) {
        final List<SeedmoneyTransaction> transactions =
                seedmoneyTransactionRepository.findByUserIdAndTransactionType(userId, "SAVE");
        return transactions.stream()
                .filter(t -> isMatchingPassSave(subscription, t))
                .mapToInt(SeedmoneyTransaction::getAmount)
                .sum();
    }

    /** 이번 주 월~일 중 어느 날 저축했는지 boolean 배열 반환 */
    private List<Boolean> calculateWeeklyHistory(final Long userId, final PassSubscription subscription) {
        final LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        final LocalDateTime weekStart = monday.atStartOfDay();

        final List<SeedmoneyTransaction> transactions =
                seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(
                        userId, "SAVE", weekStart);

        final Set<DayOfWeek> savedDays = transactions.stream()
                .filter(t -> isMatchingPassSave(subscription, t))
                .map(t -> t.getCreatedAt().getDayOfWeek())
                .collect(Collectors.toSet());

        final List<Boolean> weeklyHistory = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weeklyHistory.add(savedDays.contains(DayOfWeek.of(i + 1)));
        }
        return weeklyHistory;
    }

    private boolean isMatchingPassSave(final PassSubscription subscription, final SeedmoneyTransaction transaction) {
        if (subscription.getId() != null && subscription.getId().equals(transaction.getPassId())) {
            return true;
        }

        return subscription.getPassProduct().getId().equals(transaction.getPassId());
    }

    private String resolveSourceAccountNo(final UserAccount mainAccount, final String sourceAccountId) {
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new IllegalArgumentException("출금 계좌번호는 필수입니다.");
        }
        if (!mainAccount.getAccountNumber().equals(sourceAccountId)) {
            throw new IllegalArgumentException("출금 계좌는 주계좌만 사용할 수 있습니다.");
        }
        return sourceAccountId;
    }
}
