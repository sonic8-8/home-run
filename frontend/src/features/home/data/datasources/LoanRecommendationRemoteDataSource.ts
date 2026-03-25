import { apiClient } from '@core/network/apiClient';
import type { LoanRecommendationResponseModel } from '../models/LoanRecommendationModel';

export class LoanRecommendationRemoteDataSource {
  getRecommendations(): Promise<LoanRecommendationResponseModel> {
    return apiClient.get('/api/home/loan-recommendations');
  }
}
