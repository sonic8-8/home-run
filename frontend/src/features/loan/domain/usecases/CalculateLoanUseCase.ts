import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanCalculateParams, LoanCalculation } from '../entities/LoanCalculation';

@injectable()
export class CalculateLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, params: LoanCalculateParams): Promise<LoanCalculation> {
    return this.repository.calculate(sessionId, params);
  }
}
