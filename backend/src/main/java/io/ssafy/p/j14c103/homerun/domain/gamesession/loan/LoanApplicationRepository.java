package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {

    List<LoanApplication> findAllByGameSessionId(Integer gameSessionId);
}
