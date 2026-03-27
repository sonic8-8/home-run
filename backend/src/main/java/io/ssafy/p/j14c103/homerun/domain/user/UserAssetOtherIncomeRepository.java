package io.ssafy.p.j14c103.homerun.domain.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAssetOtherIncomeRepository extends JpaRepository<UserAssetOtherIncome, Long> {

    List<UserAssetOtherIncome> findAllByUserIdOrderByIdAsc(Long userId);

    void deleteByUserId(Long userId);
}
