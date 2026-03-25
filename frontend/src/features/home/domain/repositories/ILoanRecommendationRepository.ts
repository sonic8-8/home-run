import type { LoanRecommendationData } from '../entities/LoanRecommendation';

export interface ILoanRecommendationRepository {
  getRecommendations(): Promise<LoanRecommendationData>;
}
