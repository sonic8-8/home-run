import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanProductDetail } from '../entities/LoanProduct';

export class GetLoanProductDetailUseCase {
  private readonly repository: ILoanRepository;
  constructor(repository: ILoanRepository) { this.repository = repository; }

  async execute(sessionId: number, productId: string): Promise<LoanProductDetail> {
    return this.repository.getProductDetail(sessionId, productId);
  }
}
