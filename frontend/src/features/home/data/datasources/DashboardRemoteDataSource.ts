import { apiClient } from '@core/network/apiClient';
import type { DashboardResponseModel } from '../models/DashboardModel';

export class DashboardRemoteDataSource {
  get(): Promise<DashboardResponseModel> {
    return apiClient.get<DashboardResponseModel>('/api/home/dashboard');
  }
}
