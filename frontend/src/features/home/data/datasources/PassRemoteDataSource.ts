import { apiClient } from '@core/network/apiClient';
import type {
  PassProductsResponseModel,
  PassSubscriptionsResponseModel,
  PassSubscribeResponseModel,
  PassSaveResponseModel,
  PassWidgetModel,
  PassHistoryPageModel,
} from '../models/PassModel';

export class PassRemoteDataSource {
  getProducts(): Promise<PassProductsResponseModel> {
    return apiClient.get<PassProductsResponseModel>('/api/pass/products');
  }

  getSubscriptions(): Promise<PassSubscriptionsResponseModel> {
    return apiClient.get<PassSubscriptionsResponseModel>('/api/pass/subscriptions');
  }

  subscribe(passId: number): Promise<PassSubscribeResponseModel> {
    return apiClient.post<PassSubscribeResponseModel>('/api/pass/subscribe', { passId });
  }

  unsubscribe(subscriptionId: number): Promise<void> {
    return apiClient.delete<void>(`/api/pass/subscriptions/${subscriptionId}`);
  }

  save(subscriptionId: number): Promise<PassSaveResponseModel> {
    return apiClient.post<PassSaveResponseModel>('/api/pass/save', { subscriptionId });
  }

  getWidget(): Promise<PassWidgetModel> {
    return apiClient.get<PassWidgetModel>('/api/pass/widget');
  }

  getHistory(page: number, size: number): Promise<PassHistoryPageModel> {
    return apiClient.get<PassHistoryPageModel>(`/api/pass/history?page=${page}&size=${size}`);
  }
}
