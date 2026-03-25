package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLoanRepository extends JpaRepository<GameLoan, Integer> {

    List<GameLoan> findAllByGameSessionId(Long gameSessionId);

    List<GameLoan> findAllByGameSessionIdAndLoanStatus(Long gameSessionId, LoanStatus loanStatus);

    int countByGameSessionIdAndProductIdAndLoanStatus(
            Long gameSessionId, String productId, LoanStatus loanStatus);
}
