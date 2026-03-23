import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanRepayResult } from '../entities/ActiveLoan';

export class RepayLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(sessionId: number, loanId: number, amount: number): Promise<LoanRepayResult> {
    return this.repository.repay(sessionId, loanId, amount);
  }
}
