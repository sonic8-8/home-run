import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
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
    return apiClient.get<ApiEnvelope<PassProductsResponseModel>>('/pass/products').then(unwrapApiData);
  }

  getSubscriptions(): Promise<PassSubscriptionsResponseModel> {
    return apiClient.get<ApiEnvelope<PassSubscriptionsResponseModel>>('/pass/subscriptions').then(unwrapApiData);
  }

  subscribe(passId: number): Promise<PassSubscribeResponseModel> {
    return apiClient.post<ApiEnvelope<PassSubscribeResponseModel>>('/pass/subscribe', { passId }).then(unwrapApiData);
  }

  unsubscribe(subscriptionId: number): Promise<void> {
    return apiClient.delete(`/pass/subscriptions/${subscriptionId}`).then(() => undefined);
  }

  save(subscriptionId: number): Promise<PassSaveResponseModel> {
    return apiClient.post<ApiEnvelope<PassSaveResponseModel>>('/pass/save', { subscriptionId }).then(unwrapApiData);
  }

  getWidget(): Promise<PassWidgetModel> {
    return apiClient.get<ApiEnvelope<PassWidgetModel>>('/pass/widget').then(unwrapApiData);
  }

  getHistory(page: number, size: number): Promise<PassHistoryPageModel> {
    return apiClient.get<ApiEnvelope<PassHistoryPageModel>>(
      '/pass/history',
      { params: { page, size } },
    ).then(unwrapApiData);
  }
}
