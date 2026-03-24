package io.ssafy.p.j14c103.homerun.domain.card;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Long> {

    List<CardProduct> findByActiveYnTrueOrderByCardNameAsc();

    List<CardProduct> findTop5ByActiveYnTrueOrderByCardNameAsc();
}
