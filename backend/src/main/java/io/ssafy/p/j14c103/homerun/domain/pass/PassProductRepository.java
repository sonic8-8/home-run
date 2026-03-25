package io.ssafy.p.j14c103.homerun.domain.pass;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassProductRepository extends JpaRepository<PassProduct, Long> {

    List<PassProduct> findAllByOrderByIdAsc();
}
