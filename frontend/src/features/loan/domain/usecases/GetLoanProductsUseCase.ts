import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanCategory, LoanProductPage } from '../entities/LoanProduct';

@injectable()
export class GetLoanProductsUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, category: LoanCategory = 'ALL', page = 0, size = 10): Promise<LoanProductPage> {
    return this.repository.getProducts(sessionId, category, page, size);
  }
}
