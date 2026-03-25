import type { ICreditScoreRepository } from '../repositories/ICreditScoreRepository';
import type { CreditScore } from '../entities/CreditScore';

export class GetCreditScoreUseCase {
  private readonly repository: ICreditScoreRepository;
  constructor(repository: ICreditScoreRepository) { this.repository = repository; }

  execute(): Promise<CreditScore> {
    return this.repository.get();
  }
}
