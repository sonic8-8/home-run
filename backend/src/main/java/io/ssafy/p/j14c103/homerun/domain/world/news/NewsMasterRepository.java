package io.ssafy.p.j14c103.homerun.domain.world.news;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsMasterRepository extends JpaRepository<NewsMaster, String> {

    boolean existsByEconomicCycleType(String economicCycleType);

    List<NewsMaster> findAllByEconomicCycleTypeOrderByNewsIdAsc(String economicCycleType);
}
