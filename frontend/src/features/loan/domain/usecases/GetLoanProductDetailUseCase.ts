import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ILoanRepository } from '../repositories/ILoanRepository';
import type { LoanProductDetail } from '../entities/LoanProduct';

@injectable()
export class GetLoanProductDetailUseCase {
  private readonly repository: ILoanRepository;
  constructor(
    @inject(DI_TOKENS.ILoanRepository)
    repository: ILoanRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, productId: string): Promise<LoanProductDetail> {
    return this.repository.getProductDetail(sessionId, productId);
  }
}
