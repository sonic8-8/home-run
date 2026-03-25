import { apiClient } from '@core/network/apiClient';
import type { SpendingResponseModel } from '../models/SpendingModel';

export class SpendingRemoteDataSource {
  get(month?: string): Promise<SpendingResponseModel> {
    const query = month ? `?month=${month}` : '';
    return apiClient.get<SpendingResponseModel>(`/api/home/spending${query}`);
  }
}
