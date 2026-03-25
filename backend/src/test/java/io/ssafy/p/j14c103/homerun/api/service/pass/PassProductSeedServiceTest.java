package io.ssafy.p.j14c103.homerun.api.service.pass;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PassProductSeedServiceTest {

    @Autowired
    private PassProductSeedService passProductSeedService;

    @Autowired
    private PassProductRepository passProductRepository;

    @AfterEach
    void tearDown() {
        passProductRepository.deleteAllInBatch();
        passProductSeedService.ensureDefaultProducts();
    }

    @DisplayName("PASS 기본 상품이 비어 있으면 6종을 초기화한다")
    @Test
    void ensureDefaultProducts() {
        // given
        passProductRepository.deleteAllInBatch();

        // when
        passProductSeedService.ensureDefaultProducts();
        final List<String> productNames = passProductRepository.findAllByOrderByIdAsc().stream()
                .map(product -> product.getName())
                .toList();

        // then
        assertThat(productNames).containsExactly(
                "커피 PASS",
                "배달 PASS",
                "택시 PASS",
                "쇼핑 PASS",
                "편의점 PASS",
                "술 PASS"
        );
    }

    @DisplayName("PASS 기본 상품 초기화는 누락된 기본 상품만 채운다")
    @Test
    void ensureDefaultProducts_recoversMissingDefaultsOnly() {
        // given
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
        passProductSeedService.ensureDefaultProducts();
        final List<String> productNames = passProductRepository.findAllByOrderByIdAsc().stream()
                .map(PassProduct::getName)
                .toList();
        final long coffeePassCount = productNames.stream()
                .filter(name -> name.equals("커피 PASS"))
                .count();

        // then
        assertThat(coffeePassCount).isEqualTo(1);
        assertThat(productNames).contains("테스트 PASS");
        assertThat(productNames).contains(
                "커피 PASS",
                "배달 PASS",
                "택시 PASS",
                "쇼핑 PASS",
                "편의점 PASS",
                "술 PASS"
        );
    }
}
