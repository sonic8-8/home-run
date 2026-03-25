import type { ILoanRecommendationRepository } from '../repositories/ILoanRecommendationRepository';
import type { LoanRecommendationData } from '../entities/LoanRecommendation';

export class GetLoanRecommendationsUseCase {
  private readonly repository: ILoanRecommendationRepository;
  constructor(repository: ILoanRecommendationRepository) { this.repository = repository; }

  execute(): Promise<LoanRecommendationData> {
    return this.repository.getRecommendations();
  }
}
