import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanConfirmResult } from '../entities/ActiveLoan';

@injectable()
export class ConfirmLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(
    sessionId: number,
    applicationId: number,
    requestedAmount: number,
  ): Promise<LoanConfirmResult> {
    return this.repository.confirm(sessionId, applicationId, requestedAmount, true);
  }
}
