import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { LoanRecommendationResponseModel } from '../models/LoanRecommendationModel';

export class LoanRecommendationRemoteDataSource {
  getRecommendations(): Promise<LoanRecommendationResponseModel> {
    return apiClient.get<ApiEnvelope<LoanRecommendationResponseModel>>('/home/loan-recommendations').then(unwrapApiData);
  }
}
