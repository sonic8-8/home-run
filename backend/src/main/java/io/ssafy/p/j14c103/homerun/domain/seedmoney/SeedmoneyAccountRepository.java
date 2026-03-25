package io.ssafy.p.j14c103.homerun.domain.seedmoney;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeedmoneyAccountRepository extends JpaRepository<SeedmoneyAccount, Long> {

    Optional<SeedmoneyAccount> findByUserId(Long userId);
}
