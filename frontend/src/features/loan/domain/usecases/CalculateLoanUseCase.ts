import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanCalculateParams, LoanCalculation } from '../entities/LoanCalculation';

export class CalculateLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(sessionId: number, params: LoanCalculateParams): Promise<LoanCalculation> {
    return this.repository.calculate(sessionId, params);
  }
}
