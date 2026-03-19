package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSubscribeServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassService {

    private final PassProductRepository passProductRepository;
    private final PassSubscriptionRepository passSubscriptionRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    public List<PassProductResponse> getProducts() {
        return passProductRepository.findAll().stream()
                .map(PassProductResponse::from)
                .toList();
    }

    public List<PassSubscriptionResponse> getSubscriptions(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final List<PassSubscription> subscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);

        return subscriptions.stream()
                .map(sub -> {
                    final int totalSaved = calculateTotalSaved(userId, sub.getPassProduct().getId());
                    final List<Boolean> weeklyHistory = calculateWeeklyHistory(userId, sub.getPassProduct().getId());
                    return PassSubscriptionResponse.from(sub, totalSaved, weeklyHistory);
                })
                .toList();
    }

    @Transactional
    public PassSubscriptionResponse subscribe(final Long userId, final PassSubscribeServiceRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        final PassProduct product = passProductRepository.findById(request.getPassId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 PASS 상품입니다."));

        final PassSubscription subscription = PassSubscription.create(
                userId, product, product.getAmountPerSave(), request.getSourceAccountId());

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

    private int calculateTotalSaved(final Long userId, final Long passId) {
        final List<SeedmoneyTransaction> transactions =
                seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(
                        userId, "SAVE", LocalDateTime.MIN);
        return transactions.stream()
                .filter(t -> passId.equals(t.getPassId()))
                .mapToInt(SeedmoneyTransaction::getAmount)
                .sum();
    }

    /** 이번 주 월~일 중 어느 날 저축했는지 boolean 배열 반환 */
    private List<Boolean> calculateWeeklyHistory(final Long userId, final Long passId) {
        final LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        final LocalDateTime weekStart = monday.atStartOfDay();

        final List<SeedmoneyTransaction> transactions =
                seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(
                        userId, "SAVE", weekStart);

        final Set<DayOfWeek> savedDays = transactions.stream()
                .filter(t -> passId.equals(t.getPassId()))
                .map(t -> t.getCreatedAt().getDayOfWeek())
                .collect(Collectors.toSet());

        final List<Boolean> weeklyHistory = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weeklyHistory.add(savedDays.contains(DayOfWeek.of(i + 1)));
        }
        return weeklyHistory;
    }
}
