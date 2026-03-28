import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { DashboardResponseModel } from '../models/DashboardModel';

export class DashboardRemoteDataSource {
  get(): Promise<DashboardResponseModel> {
    return apiClient.get<ApiEnvelope<DashboardResponseModel>>('/home/dashboard').then(unwrapApiData);
  }
}
