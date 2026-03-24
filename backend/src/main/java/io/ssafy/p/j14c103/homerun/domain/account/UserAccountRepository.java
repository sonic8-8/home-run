package io.ssafy.p.j14c103.homerun.domain.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    List<UserAccount> findByUserId(Long userId);

    Optional<UserAccount> findByUserIdAndAccountType(Long userId, AccountType accountType);

    List<UserAccount> findByUserIdAndActiveYnTrue(Long userId);
}
