import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { SpendingResponseModel } from '../models/SpendingModel';

export class SpendingRemoteDataSource {
  get(month?: string): Promise<SpendingResponseModel> {
    return apiClient.get<ApiEnvelope<SpendingResponseModel>>(
      '/home/spending',
      month === undefined ? undefined : { params: { month } },
    ).then(unwrapApiData);
  }
}
