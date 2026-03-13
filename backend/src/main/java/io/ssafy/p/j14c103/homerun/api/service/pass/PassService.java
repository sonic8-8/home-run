package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSubscribeRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassService {

    private final PassProductRepository passProductRepository;
    private final PassSubscriptionRepository passSubscriptionRepository;

    public List<PassProductResponse> getProducts() {
        return passProductRepository.findAll().stream()
                .map(PassProductResponse::from)
                .toList();
    }

    public List<PassSubscriptionResponse> getSubscriptions(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(PassSubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public PassSubscriptionResponse subscribe(final PassSubscribeRequest request) {
        final PassProduct product = passProductRepository.findById(request.getPassProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 PASS 상품입니다."));

        final PassSubscription subscription = PassSubscription.create(
                request.getUserId(), product, request.getSavingAmount(), request.getSourceAccountNo());

        return PassSubscriptionResponse.from(passSubscriptionRepository.save(subscription));
    }

    @Transactional
    public void cancelSubscription(final Long subscriptionId) {
        final PassSubscription subscription = passSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다."));

        if (!subscription.getIsActive()) {
            throw new IllegalStateException("이미 해지된 구독입니다.");
        }

        subscription.cancel();
    }
}
