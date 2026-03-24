import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanCategory, LoanProductPage } from '../entities/LoanProduct';

export class GetLoanProductsUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(sessionId: number, category: LoanCategory = 'ALL', page = 0, size = 10): Promise<LoanProductPage> {
    return this.repository.getProducts(sessionId, category, page, size);
  }
}
