import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { CreditScoreResponseModel } from '../models/CreditScoreModel';

export class CreditScoreRemoteDataSource {
  get(): Promise<CreditScoreResponseModel> {
    return apiClient.get<ApiEnvelope<CreditScoreResponseModel>>('/credit/score').then(unwrapApiData);
  }
}
