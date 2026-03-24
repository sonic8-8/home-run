import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanApplication } from '../entities/LoanApplication';

export class ApplyLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(sessionId: number, productId: string, propertyId: string): Promise<LoanApplication> {
    return this.repository.apply(sessionId, productId, propertyId);
  }
}
