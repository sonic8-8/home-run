package io.ssafy.p.j14c103.homerun.api.service.pass;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.AfterEach;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PassServiceTest {

    @Autowired
    private PassService passService;

    @Autowired
    private PassProductRepository passProductRepository;

    @Autowired
    private PassSubscriptionRepository passSubscriptionRepository;

    @Autowired
    private SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PassProductSeedService passProductSeedService;

    @AfterEach
    void tearDown() {
        seedmoneyTransactionRepository.deleteAllInBatch();
        passSubscriptionRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        passProductRepository.deleteAllInBatch();
        passProductSeedService.ensureDefaultProducts();
    }

    @DisplayName("전체 PASS 상품 목록 조회 시 기본 상품 6종을 반환한다")
    @Test
    void getProducts() {
        // given
        passSubscriptionRepository.deleteAllInBatch();
        passProductRepository.deleteAllInBatch();

        // when
        final List<PassProductResponse> result = passService.getProducts();

        // then
        assertThat(result)
                .extracting(PassProductResponse::getName)
                .containsExactly(
                        "커피 PASS",
                        "배달 PASS",
                        "택시 PASS",
                        "쇼핑 PASS",
                        "편의점 PASS",
                        "술 PASS"
                );
    }

    @DisplayName("전체 PASS 상품 목록 조회 시 누락된 기본 상품만 복구한다")
    @Test
    void getProducts_recoversMissingDefaultProducts() {
        // given
        passSubscriptionRepository.deleteAllInBatch();
        passProductRepository.deleteAllInBatch();
        passProductRepository.save(PassProduct.create(
                "커피 PASS",
                5000,
                "커피 마시고 싶은 마음을 꾹 참고 저축해볼까요?"
        ));
        passProductRepository.save(PassProduct.create(
                "테스트 PASS",
                7000,
                "임시 테스트 상품"
        ));

        // when
        final List<PassProductResponse> result = passService.getProducts();

        // then
        assertThat(result).hasSize(7);
        assertThat(result)
                .extracting(PassProductResponse::getName)
                .contains(
                        "커피 PASS",
                        "배달 PASS",
                        "택시 PASS",
                        "쇼핑 PASS",
                        "편의점 PASS",
                        "술 PASS",
                        "테스트 PASS"
                );
        assertThat(result.stream()
                .filter(product -> product.getName().equals("커피 PASS"))
                .count()).isEqualTo(1);
    }

    @DisplayName("PASS 구독을 신청하면 활성 구독으로 저장된다")
    @Test
    void subscribe() {
        // given
        final PassProduct product = passProductRepository.save(PassProduct.create(
                "커피 PASS",
                5000,
                "커피 한 잔 절약"
        ));
        userAccountRepository.save(UserAccount.create(
                1L,
                AccountType.MAIN,
                "001",
                "한국은행",
                "0012345678",
                100000
        ));
        final PassSubscribeServiceRequest request = PassSubscribeServiceRequest.builder()
                .passId(product.getId())
                .build();

        // when
        final PassSubscriptionResponse result = passService.subscribe(1L, request);

        // then
        final PassSubscription savedSubscription = passSubscriptionRepository.findById(result.getSubscriptionId())
                .orElseThrow();
        assertThat(savedSubscription.getIsActive()).isTrue();
        assertThat(savedSubscription.getSourceAccountNo()).isEqualTo("0012345678");
        assertThat(savedSubscription.getSavingAmount()).isEqualTo(5000);
        assertThat(result.getName()).isEqualTo("커피 PASS");
    }

    @DisplayName("사용자의 활성 PASS 구독 목록 조회 시 누적 저축 금액과 주간 이력을 함께 반환한다")
    @Test
    void getSubscriptions() {
        // given
        final PassProduct product = passProductRepository.save(PassProduct.create(
                "커피 PASS",
                5000,
                "커피 한 잔 절약"
        ));
        final PassSubscription subscription = passSubscriptionRepository.save(
                PassSubscription.create(1L, product, 5000, "0012345678")
        );
        seedmoneyTransactionRepository.save(SeedmoneyTransaction.createSave(1L, product.getId(), 5000));

        // when
        final List<PassSubscriptionResponse> result = passService.getSubscriptions(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("커피 PASS");
        assertThat(result.get(0).getAmountPerSave()).isEqualTo(5000);
        assertThat(result.get(0).getTotalSaved()).isEqualTo(5000);
        assertThat(result.get(0).getWeeklyHistory()).hasSize(7);
        assertThat(result.get(0).getWeeklyHistory()).contains(true);
    }

@DisplayName("PASS 구독을 해지하면 내 PASS 목록에서 제외된다")
    @Test
    void cancelSubscription() {
        // given
        final PassProduct product = passProductRepository.save(PassProduct.create(
                "커피 PASS",
                5000,
                "커피 한 잔 절약"
        ));
        final PassSubscription subscription = passSubscriptionRepository.save(
                PassSubscription.create(1L, product, 5000, "0012345678")
        );

        // when
        passService.cancelSubscription(1L, subscription.getId());
        final List<PassSubscriptionResponse> result = passService.getSubscriptions(1L);

        // then
        assertThat(passSubscriptionRepository.findById(subscription.getId()).orElseThrow().getIsActive()).isFalse();
        assertThat(result).isEmpty();
    }
}
