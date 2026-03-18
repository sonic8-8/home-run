package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSubscribeRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassProductResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSubscriptionResponse;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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

    @Mock
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @InjectMocks
    private PassService passService;

  @DisplayName("전체 PASS 상품 목록을 조회한다")
  @Test
  void getProducts() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");

    given(passProductRepository.findAll()).willReturn(List.of(product));

    // when
    final List<PassProductResponse> result = passService.getProducts();

    // then
    assertThat(result).hasSize(1);
  }

  @DisplayName("사용자의 활성 구독 목록을 조회한다")
  @Test
  void getSubscriptions() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, product, 5000, "0012345678");

    given(passSubscriptionRepository.findByUserIdAndIsActiveTrue(1L))
            .willReturn(List.of(subscription));
    given(seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtAfter(any(), any(), any()))
            .willReturn(Collections.emptyList());

    // when
    final List<PassSubscriptionResponse> result = passService.getSubscriptions(1L);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("커피 PASS");
        assertThat(result.get(0).getAmountPerSave()).isEqualTo(5000);
        assertThat(result.get(0).getTotalSaved()).isEqualTo(0);
        assertThat(result.get(0).getWeeklyHistory()).hasSize(7);
    }

  @DisplayName("PASS 구독을 신청한다")
  @Test
  void subscribe() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscribeRequest request = new PassSubscribeRequest(1L, "0012345678");

    given(passProductRepository.findById(1L)).willReturn(Optional.of(product));
    given(passSubscriptionRepository.save(any(PassSubscription.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

    // when
    final PassSubscriptionResponse result = passService.subscribe(1L, request);

    // then
    assertThat(result.getName()).isEqualTo("커피 PASS");
    assertThat(result.getAmountPerSave()).isEqualTo(5000);
  }

  @DisplayName("존재하지 않는 상품으로 구독하면 예외가 발생한다")
  @Test
  void subscribe_notFound_exception() {
    // given
    final PassSubscribeRequest request = new PassSubscribeRequest(999L, "0012345678");

    given(passProductRepository.findById(999L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> passService.subscribe(1L, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 PASS 상품");
    }

  @DisplayName("사용자 ID가 null이면 구독 목록 조회 시 예외가 발생한다")
  @Test
  void getSubscriptions_nullUserId_exception() {
    // when & then
    assertThatThrownBy(() -> passService.getSubscriptions(null))
            .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("구독을 해지한다")
  @Test
  void cancelSubscription() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, product, 5000, "0012345678");

    given(passSubscriptionRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(subscription));

    // when
    passService.cancelSubscription(1L, 1L);

    // then
    assertThat(subscription.getIsActive()).isFalse();
  }

  @DisplayName("이미 해지된 구독을 다시 해지하면 예외가 발생한다")
  @Test
  void cancelSubscription_alreadyCanceled_exception() {
    // given
    final PassProduct product = PassProduct.create("커피 PASS", 5000, "커피 한 잔 절약");
    final PassSubscription subscription = PassSubscription.create(1L, product, 5000, "0012345678");
    subscription.cancel();

    given(passSubscriptionRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(subscription));

    // when & then
    assertThatThrownBy(() -> passService.cancelSubscription(1L, 1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 해지된 구독");
    }

  @DisplayName("다른 사용자의 구독은 해지할 수 없다")
  @Test
  void cancelSubscription_otherUsersSubscription_exception() {
    // given
    given(passSubscriptionRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> passService.cancelSubscription(1L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 구독");
    }
}
