package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSubscribeRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PassServiceTest {

    @Mock
    private PassProductRepository passProductRepository;

    @Mock
    private PassSubscriptionRepository passSubscriptionRepository;

    @InjectMocks
    private PassService passService;

    @DisplayName("전체 PASS 상품 목록을 조회한다")
    @Test
    void getProducts() {
        final PassProduct product = PassProduct.create("커피 PASS", "커피 한 잔 절약");

        given(passProductRepository.findAll()).willReturn(List.of(product));

        final List<PassProductResponse> result = passService.getProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("커피 PASS");
    }

    @DisplayName("사용자의 활성 구독 목록을 조회한다")
    @Test
    void getSubscriptions() {
        final PassProduct product = PassProduct.create("커피 PASS", "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, product, 4500, "0012345678");

        given(passSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
                .willReturn(List.of(subscription));

        final List<PassSubscriptionResponse> result = passService.getSubscriptions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassName()).isEqualTo("커피 PASS");
        assertThat(result.get(0).isActive()).isTrue();
    }

    @DisplayName("PASS 구독을 신청한다")
    @Test
    void subscribe() {
        final PassProduct product = PassProduct.create("커피 PASS", "커피 한 잔 절약");
        final PassSubscribeRequest request = new PassSubscribeRequest(1L, 1L, 4500, "0012345678", "test-key");

        given(passProductRepository.findById(1L)).willReturn(Optional.of(product));
        given(passSubscriptionRepository.save(any(PassSubscription.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final PassSubscriptionResponse result = passService.subscribe(request);

        assertThat(result.getPassName()).isEqualTo("커피 PASS");
        assertThat(result.isActive()).isTrue();
    }

    @DisplayName("존재하지 않는 상품으로 구독 신청 시 예외가 발생한다")
    @Test
    void subscribe_invalidProduct_exception() {
        final PassSubscribeRequest request = new PassSubscribeRequest(1L, 999L, 4500, "0012345678", "test-key");

        given(passProductRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> passService.subscribe(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 PASS 상품");
    }

    @DisplayName("구독을 해지한다")
    @Test
    void cancelSubscription() {
        final PassProduct product = PassProduct.create("커피 PASS", "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, product, 4500, "0012345678");

        given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        passService.cancelSubscription(1L);

        assertThat(subscription.getIsActive()).isFalse();
    }

    @DisplayName("이미 해지된 구독을 다시 해지하면 예외가 발생한다")
    @Test
    void cancelSubscription_alreadyCanceled_exception() {
        final PassProduct product = PassProduct.create("커피 PASS", "커피 한 잔 절약");
        final PassSubscription subscription = PassSubscription.create(1L, product, 4500, "0012345678");
        subscription.cancel();

        given(passSubscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        assertThatThrownBy(() -> passService.cancelSubscription(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 해지된 구독");
    }

    @DisplayName("사용자 ID가 null이면 예외가 발생한다")
    @Test
    void getSubscriptions_nullUserId_exception() {
        assertThatThrownBy(() -> passService.getSubscriptions(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
