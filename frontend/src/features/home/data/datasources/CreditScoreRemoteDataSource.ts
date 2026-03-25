import { apiClient } from '@core/network/apiClient';
import type { CreditScoreResponseModel } from '../models/CreditScoreModel';

export class CreditScoreRemoteDataSource {
  get(): Promise<CreditScoreResponseModel> {
    return apiClient.get('/api/credit/score');
  }
}
