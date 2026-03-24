package io.ssafy.p.j14c103.homerun.domain.world.housing.trade;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentTradeRawRepository extends JpaRepository<ApartmentTradeRaw, Long> {

    Optional<ApartmentTradeRaw> findByTradeKey(String tradeKey);
}
