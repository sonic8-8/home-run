package io.ssafy.p.j14c103.homerun.domain.financial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFinancialProductRepository extends JpaRepository<UserFinancialProduct, Long> {

    List<UserFinancialProduct> findByUserIdAndActiveYnTrue(Long userId);

    boolean existsByUserIdAndActiveYnTrue(Long userId);
}
