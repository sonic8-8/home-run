package io.ssafy.p.j14c103.homerun.domain.financial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialProductTemplateRepository extends JpaRepository<FinancialProductTemplate, Long> {

    List<FinancialProductTemplate> findAllByProductTypeAndActiveYnTrueOrderByIdAsc(FinancialProductType productType);
}
