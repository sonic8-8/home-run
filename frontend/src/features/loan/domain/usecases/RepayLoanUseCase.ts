import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanRepayResult } from '../entities/ActiveLoan';

@injectable()
export class RepayLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, loanId: number, amount: number): Promise<LoanRepayResult> {
    return this.repository.repay(sessionId, loanId, amount);
  }
}
