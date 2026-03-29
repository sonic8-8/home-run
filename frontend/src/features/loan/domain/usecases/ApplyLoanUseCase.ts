import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanApplication } from '../entities/LoanApplication';

@injectable()
export class ApplyLoanUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, productId: string, propertyId: string): Promise<LoanApplication> {
    return this.repository.apply(sessionId, productId, propertyId);
  }
}
