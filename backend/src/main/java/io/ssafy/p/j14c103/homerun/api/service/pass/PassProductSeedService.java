package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.domain.pass.PassProduct;
import io.ssafy.p.j14c103.homerun.domain.pass.PassProductRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PassProductSeedService {

    private static final List<DefaultPassProduct> DEFAULT_PRODUCTS = List.of(
            DefaultPassProduct.of("커피 PASS", 5_000L, "커피 마시고 싶은 마음을 꾹 참고 저축해볼까요?"),
            DefaultPassProduct.of("배달 PASS", 20_000L, "배달 시키고 싶은 마음을 꾹 참고 저축해볼까요?"),
            DefaultPassProduct.of("택시 PASS", 10_000L, "택시 타고 싶은 마음을 꾹 참고 저축해볼까요?"),
            DefaultPassProduct.of("쇼핑 PASS", 15_000L, "충동구매 하고 싶은 마음을 꾹 참고 저축해볼까요?"),
            DefaultPassProduct.of("편의점 PASS", 3_000L, "편의점에서 사고 싶은 마음을 꾹 참고 저축해볼까요?"),
            DefaultPassProduct.of("술 PASS", 30_000L, "한 잔 하고 싶은 마음을 꾹 참고 저축해볼까요?")
    );

    private final PassProductRepository passProductRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeDefaultProducts() {
        syncDefaultProducts();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureDefaultProducts() {
        syncDefaultProducts();
    }

    private void syncDefaultProducts() {
        final Set<String> existingProductNames = passProductRepository.findAllByOrderByIdAsc().stream()
                .map(PassProduct::getName)
                .collect(java.util.stream.Collectors.toSet());

        final List<PassProduct> missingProducts = DEFAULT_PRODUCTS.stream()
                .filter(product -> !existingProductNames.contains(product.name()))
                .map(product -> PassProduct.create(
                        product.name(),
                        product.amountPerSave(),
                        product.description()
                ))
                .toList();

        if (missingProducts.isEmpty()) {
            return;
        }

        passProductRepository.saveAll(missingProducts);
    }

    private record DefaultPassProduct(
            String name,
            Long amountPerSave,
            String description
    ) {
        private static DefaultPassProduct of(
                final String name,
                final Long amountPerSave,
                final String description
        ) {
            return new DefaultPassProduct(name, amountPerSave, description);
        }
    }
}
