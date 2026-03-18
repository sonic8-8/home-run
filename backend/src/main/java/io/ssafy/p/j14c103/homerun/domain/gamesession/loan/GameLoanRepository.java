package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLoanRepository extends JpaRepository<GameLoan, Integer> {

    List<GameLoan> findAllByGameSessionId(Integer gameSessionId);

    List<GameLoan> findAllByGameSessionIdAndLoanStatus(Integer gameSessionId, LoanStatus loanStatus);

    int countByGameSessionIdAndProductIdAndLoanStatus(
            Integer gameSessionId, String productId, LoanStatus loanStatus);
}
