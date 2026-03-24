import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanConfirmResult } from '../entities/ActiveLoan';

export class ConfirmLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(
    sessionId: number,
    applicationId: string,
    requestedAmount: number,
  ): Promise<LoanConfirmResult> {
    return this.repository.confirm(sessionId, applicationId, requestedAmount, true);
  }
}
